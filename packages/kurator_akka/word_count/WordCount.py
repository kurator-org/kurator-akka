import collections

class TextProcessor(object):

    def next_alpha(self, s, begin):
        """Return index of first alphabetic character, starting search at begin.  Return -1 on failure."""
        i = begin
        while i < len(s) and not s[i].isalpha(): i += 1
        return i if i < len(s) else -1

    def next_non_alpha(self, text, begin):
        """Return index of first non-alphabetic character, starting search at begin.  Return -1 on failure."""
        i = begin
        while i < len(text) and text[i].isalpha(): i += 1
        return i if i < len(text) else -1

class TextChunker(TextProcessor):
    """
    Class for splitting a text string into a specified number of chunks.
    """

    def __init__(self):
        self.max_chunks=1
        self.text_index=0

    def split_text(self, text):
        """ Split text into as many as max_chunks chunks. Yields each chunk separately."""

        text_size = len(text)
        chunk_size = text_size/self.max_chunks
        chunk_start = 0

        for chunk_id in range(1, self.max_chunks):
            chunk_end = self.next_non_alpha(text, chunk_start + chunk_size)
            if chunk_end == -1:
                yield text[chunk_start:text_size]
                return
            else:
                yield text[chunk_start:chunk_end]
                chunk_start = chunk_end

        yield text[chunk_start:text_size]

    def split_text_exact_chunks(self, text):
        """ Split text into exactly max_chunks chunks. Yields each chunk separately.
        If max_chunks is greater than the number of words in text
        
    """
        chunk_index = 1
        for text_chunk in self.split_text(text):
            yield text_chunk
            chunk_index += 1
        while chunk_index <= self.max_chunks:
            yield ''
            chunk_index += 1

    def split_text_with_counts(self, text):
        self.text_index+=1
        for chunk_id, text_chunk in enumerate(self.split_text_exact_chunks(text)):
            yield self.text_index, chunk_id+1, self.max_chunks, text_chunk

class WordCounter(TextProcessor):

    def count_words(self, text):

        word_counts = collections.OrderedDict()
        start = 0
        text_size = len(text)

        while (start < text_size - 1):
    
            word_start = self.next_alpha(text, start)
            if (word_start == -1): break
    
            word_end = self.next_non_alpha(text, word_start)
            if word_end == -1: word_end = text_size   
            word = text[word_start:word_end].lower()
    
            if (word_counts.has_key(word)):
                word_counts[word] += 1
            else:
                word_counts[word] = 1
    
            start = word_end

        return word_counts        
        
    def count_words_in_chunk(self, chunk_tuple):
        return chunk_tuple[0], chunk_tuple[1], chunk_tuple[2], self.count_words(chunk_tuple[3])

class WordCountReducer(TextProcessor):

    def __init__(self):
        self.merged_counts=collections.OrderedDict()
        self.packet_count = 0
        
    def reduce_word_counts(self, word_count_tuple):

        text_id, chunk_id, chunk_count, word_counts = word_count_tuple

        for word, count in word_counts.iteritems():
            if (self.merged_counts.has_key(word)):
                self.merged_counts[word] += count
            else:
                self.merged_counts[word] = count
        
        self.packet_count += 1

        if self.packet_count == chunk_count:
            word_counts = self.merged_counts
            self.packet_count = 0
            self.merged_counts=collections.OrderedDict()
            return text_id, word_counts

    def reduce_word_counts_sorted(self, word_count_tuple):
    
         unsorted_word_count_tuple = self.reduce_word_counts(word_count_tuple)
         
         if unsorted_word_count_tuple != None:
            text_id, unsorted_counts = unsorted_word_count_tuple
            sorted_counts = collections.OrderedDict()
            for word in sorted(unsorted_counts):
                sorted_counts[word] = unsorted_counts[word]
            return text_id, sorted_counts    

if __name__ == '__main__':

    tc = TextChunker()

    text = "= _#Every _ good_ boy _ _ _ _ does _,@@# fine.."

    for max_chunks in range (1, 20):
        tc.max_chunks = max_chunks
        chunks = list(tc.split_text_with_counts(text))
        print chunks

    text = "= _#Every, every _ good, good, good, boy _ _ _ _ does _,@@# fine.."

    wc = WordCounter()
    counts =  wc.count_words(text)
    print counts

    ca = WordCountReducer()
    print ca.reduce_word_counts_sorted((1,1,3,counts))
    print ca.reduce_word_counts_sorted((1,2,3,counts))
    print ca.reduce_word_counts_sorted((1,3,3,counts))
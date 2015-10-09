def next_alpha(text, start):
    i = start
    while i < len(text) and not text[i].isalpha(): i += 1
    return i

def next_non_alpha(text, start):
    i = start
    while i < len(text) and text[i].isalpha(): i += 1
    return i

def split_text(text, max_chunks=1):

    text_size = len(text)
    chunk_size = text_size/max_chunks
    chunk_start = 0

    for chunk in range(1, max_chunks):
        chunk_end = next_non_alpha(text, chunk_start + chunk_size)
        if chunk_end >= text_size:
            yield text[chunk_start:text_size]
            return
        else:
            yield text[chunk_start:chunk_end]
            chunk_start = chunk_end

    yield text[chunk_start:text_size]


def count_words(text):

    count = {}
    start = 0
    text_size = len(text)
    
    while (start < text_size):
    
        word_start = next_alpha(text, start)
        if (word_start >= text_size): break
    
        word_end = next_non_alpha(text, word_start)
        if (word_end == text_size): word_end = text_size - 1
    
        word = text[word_start:word_end].lower()
    
        if (count.has_key(word)):
            count[word] += 1
        else:
            count[word] = 1
    
        start = word_end

    return count

def merge_counts(counts):
    merged = {}
    for i in range(0, len(counts)):
        count = counts[i]
        for word, word_count in count.iteritems():
            if (merged.has_key(word)):
                merged[word] += word_count
            else:
                merged[word] = word_count
    return merged
    
if __name__ == '__main__':
    text = "= _#Every _ good_ boy _ _ _ _ does _,@@# fine.."
    for max_chunks in range (1, 20):
        chunks = list(split_text(text, max_chunks))
        print max_chunks, chunks, len(chunks)

    text = "= _#Every, every _ good, good, good, boy _ _ _ _ does _,@@# fine.."
    counts =  count_words(text)
    print counts
    merged = merge_counts([counts, counts, counts])
    print merged
    



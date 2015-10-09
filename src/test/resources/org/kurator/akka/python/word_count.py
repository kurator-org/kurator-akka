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


if __name__ == '__main__':
    text = "= _#Every _ good_ boy _ _ _ _ does _,@@# fine.."
    for max_chunks in range (1, 20):
        chunks = list(split_text(text, max_chunks))
        print max_chunks, chunks, len(chunks)

    



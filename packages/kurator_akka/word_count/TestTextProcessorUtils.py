from WordCount import TextProcessor
import unittest

class TestStringUtils_SpacedStringBuilder(unittest.TestCase):

    def setUp(self):
        self.tp = TextProcessor()

    def test_next_alpha_first_char_is_alpha(self):
        text = 'The'
        self.assertEqual(0, self.tp.next_alpha(text, 0))

    def test_next_alpha_spaces_before_first_alpha(self):
        text = '   The'
        self.assertEqual(3, self.tp.next_alpha(text, 0))

    def test_next_alpha_punct_before_first_alpha(self):
        text = ',.!-_The'
        self.assertEqual(5, self.tp.next_alpha(text, 0))

    def test_next_alpha_last_char_is_alpha(self):
        text = 'The'
        self.assertEqual(2, self.tp.next_alpha(text, 2))
        
    def test_next_alpha_failure_last_char_is_space(self):
        text = 'The '
        self.assertEqual(-1, self.tp.next_alpha(text, 3))

    def test_next_alpha_failure_last_chars_are_spaces(self):
        text = 'The    '
        self.assertEqual(-1, self.tp.next_alpha(text, 3))        
        
    def test_next_alpha_failure_last_chars_are_punct(self):
        text = 'The....'
        self.assertEqual(-1, self.tp.next_alpha(text, 3))
        
    def test_next_alpha_start_past_end(self):
        text = 'The'
        self.assertEqual(-1, self.tp.next_alpha(text, 3))

    def test_next_non_alpha_first_char_is_space(self):
        text = ' The'
        self.assertEqual(0, self.tp.next_non_alpha(text, 0))

    def test_next_non_alpha_spaces_before_first_non_alpha(self):
        text = 'The   '
        self.assertEqual(3, self.tp.next_non_alpha(text, 0))

    def test_next_non_alpha_last_char_is_non_alpha(self):
        text = 'The '
        self.assertEqual(3, self.tp.next_non_alpha(text, 3))

    def test_next_non_alpha_failure_last_char_is_space(self):
        text = 'The'
        self.assertEqual(-1, self.tp.next_non_alpha(text, 2))

    def test_next_non_alpha_failure_all_characters_are_alphas(self):
        text = 'The'
        self.assertEqual(-1, self.tp.next_non_alpha(text, 0))

    def test_next_non_alpha_start_past_end(self):
        text = 'The'
        self.assertEqual(-1, self.tp.next_non_alpha(text, 3))
        
if __name__ == '__main__':
    unittest.main()

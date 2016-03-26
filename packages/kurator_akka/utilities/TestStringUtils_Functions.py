from StringUtils import is_blank
from StringUtils import has_content
import unittest

class TestStringUtils_Functions(unittest.TestCase):

    # tests of is_blank()

    def test_is_blank_raises_attribute_error_for_none(self):
        self.assertRaises(AttributeError, is_blank, None)

    def test_is_blank_returns_true_for_empty_string(self):
        self.assertTrue(is_blank(''))

    def test_is_blank_returns_true_for_one_space(self):
        self.assertTrue(is_blank(' '))

    def test_is_blank_returns_true_for_several_spaces(self):
        self.assertTrue(is_blank('    '))

    def test_is_blank_returns_false_for_one_nonwhitespace_char(self):
        self.assertFalse(is_blank('n'))

    def test_is_blank_returns_false_for_one_nonwhitespace_char_with_whitespace(self):
        self.assertFalse(is_blank('  n   '))

    def test_is_blank_returns_false_for_text_with_no_trimmable_whitespace(self):
        self.assertFalse(is_blank('some text'))

    def test_is_blank_returns_false_for_text_with_trimmable_whitespace(self):
        self.assertFalse(is_blank('  some text   '))

    # tests of has_content()

    def test_has_content_returns_false_for_none(self):
        self.assertFalse(has_content(None))

    def test_has_content_returns_false_for_empty_string(self):
        self.assertFalse(has_content(''))

    def test_has_content_returns_false_for_one_space(self):
        self.assertFalse(has_content(' '))

    def test_has_content_returns_false_for_multiple_spaces(self):
        self.assertFalse(has_content('    '))

    def test_has_content_returns_true_for_one_nonwhitespace_char(self):
        self.assertTrue(has_content('n'))

    def test_has_content_returns_true_for_one_nonwhitespace_char_with_whitespace(self):
        self.assertTrue(has_content('  n   '))

    def test_has_content_returns_true_for_text_with_no_trimmable_whitespace(self):
        self.assertTrue(has_content('some text'))

    def test_has_content_returns_true_for_text_with_trimmable_whitespace(self):
        self.assertTrue(has_content('  some text   '))


if __name__ == '__main__':
    unittest.main()
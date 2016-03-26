from StringUtils import SpacedStringBuilder
import unittest

class TestStringUtils_SpacedStringBuilder(unittest.TestCase):

    def test_with_default_spacer_str_returns_empty_string_for_new_instance(self):
        self.assertEqual('', str(SpacedStringBuilder()))

    def test_with_default_spacer_str_returns_original_if_single_token_appended(self):
        self.assertEqual('token', str(SpacedStringBuilder()
                                        .append("token")))

    def test_with_default_spacer_str_returns_spaced_tokens_if_two_tokens_appended(self):
        self.assertEqual('token1 token2', str(SpacedStringBuilder()
                                                .append("token1")
                                                .append("token2")))
        
    def test_with_default_spacer_str_returns_spaced_tokens_if_three_tokens_appended(self):
        self.assertEqual('token1 token2 token3', str(SpacedStringBuilder()
                                                        .append("token1")
                                                        .append("token2")
                                                        .append("token3")))

    def test_with_empty_spacer_str_returns_empty_string_for_new_instance(self):
        self.assertEqual('', str(SpacedStringBuilder('')))

    def test_with_empty_spacer_str_returns_original_if_single_token_appended(self):
        self.assertEqual('token', str(SpacedStringBuilder('')
                                        .append("token")))

    def test_with_empty_spacer_str_returns_unspaced_tokens_if_two_tokens_appended(self):
        self.assertEqual('token1token2', str(SpacedStringBuilder('')
                                                .append("token1")
                                                .append("token2")))

    def test_with_empty_spacer_str_returns_unspaced_tokens_if_three_tokens_appended(self):
        self.assertEqual('token1token2token3', str(SpacedStringBuilder('')
                                                    .append("token1")
                                                    .append("token2")
                                                    .append("token3")))

    def test_with_dot_spacer_str_returns_empty_string_for_new_instance(self):
        self.assertEqual('', str(SpacedStringBuilder('.')))

    def test_with_dot_spacer_str_returns_original_if_single_token_appended(self):
        self.assertEqual('token', str(SpacedStringBuilder('.')
                                        .append("token")))

    def test_with_dot_spacer_str_returns_dot_separated_tokens_if_two_tokens_appended(self):
        self.assertEqual('token1.token2', str(SpacedStringBuilder('.')
                                                .append("token1")
                                                .append("token2")))

    def test_with_dot_spacer_str_returns_dot_separated_tokens_if_three_tokens_appended(self):
        self.assertEqual('token1.token2.token3', str(SpacedStringBuilder('.')
                                                        .append("token1")
                                                        .append("token2")
                                                        .append("token3")))

    def test_with_default_quote_str_returns_doubly_quoted_single_token(self):
        self.assertEqual('"token"', str(SpacedStringBuilder()
                                            .append_quoted("token")))

    def test_with_single_quote_str_returns_singly_quoted_single_token(self):
        self.assertEqual("'token'", str(SpacedStringBuilder(quote="'")
                                            .append_quoted("token")))

    def test_with_default_quote_str_returns_two_doubly_quoted_tokens(self):
        self.assertEqual('"token1" "token2"', str(SpacedStringBuilder()
                                                    .append_quoted("token1")
                                                    .append_quoted("token2")))

    def test_with_single_quote_str_returns_two_singly_quoted_tokens(self):
        self.assertEqual("'token1' 'token2'", str(SpacedStringBuilder(quote="'")
                                                    .append_quoted("token1")
                                                    .append_quoted("token2")))

    def test_with_default_quote_str_returns_quoted_and_unquoted_tokens(self):
        self.assertEqual('"token1" token2 "token3"', str(SpacedStringBuilder()
                                                            .append_quoted("token1")
                                                            .append("token2")
                                                            .append_quoted("token3")))

    def test_with_single_quote_str_returns_quoted_and_unquoted_tokens(self):
        self.assertEqual("'token1' token2 'token3'", str(SpacedStringBuilder(quote="'")
                                                            .append_quoted("token1")
                                                            .append("token2")
                                                            .append_quoted("token3")))

    def test_with_comma_spacer_str_returns_comma_separated_tokens(self):
        self.assertEqual("'token1', token2, 'token3'", str(SpacedStringBuilder(quote="'", spacer=', ')
                                                            .append_quoted("token1")
                                                            .append("token2")
                                                            .append_quoted("token3")))

if __name__ == '__main__':
    unittest.main()
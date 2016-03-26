"""
Utility functions and classes for working with strings read from and written to text data 
records.
"""

def is_blank(s):
    """Return True if s is empty or contains only whitespace."""
    return len(s.strip()) == 0

def has_content(s):
    """Return True if s is not null, not empty, and contains non-whitespace characters."""
    return (s is not None) and (not is_blank(s))

class SpacedStringBuilder(object):
    """
    A string builder that automatically separates appended strings with a spacer.
    
    This class provides methods for appending strings. These strings are joined to each 
    other using the string assigned to the spacer attribute when the str() function is 
    called. The append methods silently discard strings that are null (have value None), 
    empty (contain no characters) or have no printable content (contain only whitespace 
    characters). The append methods return self so that calls to these functions can be 
    chained.
    """

    def __init__(self, spacer=' ', quote='"'):
        """
        Initialize an automatically spaced string builder.
        
        Keyword arguments:
        spacer -- string used to join appended strings (default is one space)
        quote  -- string used bracket strings appended with append_quoted() (default is ")
        """
        self._str_list = []
        self._spacer = spacer
        self._quote = quote
  
    def append(self, s):
        """Append s if s has non-whitespace content."""
        if has_content(s):
            self._str_list.append(s)
        return self

    def append_quoted(self, s): 
        """Append s surrounded by quotes if s has non-whitespace content."""
        if has_content(s):
            self._str_list.append(self._quote + s + self._quote)
        return self
    
    def __repr__(self):
        """Return the current string.  Called by str()."""
        return self._spacer.join(self._str_list)


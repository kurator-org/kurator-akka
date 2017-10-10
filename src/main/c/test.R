# This function is invoked by the C program r_test

hello <- function(a) {
  cat("R received: ", a, "\n");

  return(a + 1)
}

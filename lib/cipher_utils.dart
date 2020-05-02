class CipherUtils {
  static String removeNewLines(String multilineString) {
    return multilineString.replaceAll('\n', '');
  }
}

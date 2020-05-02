import 'package:cipher/cipher_utils.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('CipherUtils', () {
    group('removeNewLines', () {
      test(
          'should remove only the new lines and return '
          'single line new string when multiline is passed', () {
        // given
        const String multilineString = 'SOME'
            'String\nline';
        expect(multilineString.contains('\n'), true);

        // when
        final String result = CipherUtils.removeNewLines(multilineString);

        //then
        expect(result, 'SOMEStringline');
        expect(result.contains('\n'), false);
      });

      test(
          'should return the string as is'
          'when there is no new line character', () {
        // given
        const String multilineString = 'SOMEStringline';
        expect(multilineString.contains('\n'), false);

        // when
        final String result = CipherUtils.removeNewLines(multilineString);

        //then
        expect(result, 'SOMEStringline');
        expect(result.contains('\n'), false);
      });
    });
  });
}

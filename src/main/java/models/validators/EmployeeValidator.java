package models.validators;

import java.util.ArrayList;
import java.util.List;

import actions.views.EmployeeView;
import constants.MessageConst;
import services.EmployeeService;

/*
 * 従業員インスタンスの各値のバリデーションを行う
 */
public class EmployeeValidator {

    /*
     * 従業員インスタンスの各項目についてバリデーションを行う
     *
     * @param service 呼び出し元Serviceクラスのインスタンス
     * @param ev EmployeeServiceのインスタンス
     * @param codeDuplicateCheckFlag 社員番号の重複チェックを実施するかどうか(実施する:true 実施しない:false)
     * @param passwordCheckFlag パスワードの入力チェックを実施するかどうか(実施する:true 実施しない:false)
     * @return エラーのリスト
     */
    public static List<String> validate(
            EmployeeService service, EmployeeView ev, Boolean codeDuplicateCheckFlag, Boolean passwordCheckFlag) {
        List<String> errors = new ArrayList<String>();

        // 社員番号のチェック
        String codeError = validateCode(service, ev.getCode(), codeDuplicateCheckFlag);
        if (!codeError.equals("")) {
            errors.add(codeError);
        }

        // 氏名のチェック
        String nameError = validateName(service, ev.getName());
        if (!nameError.equals("")) {
            errors.add(codeError);
        }

        // パスワードのチェック
        String passError = validatePassword(service, ev.getPassword(), passwordCheckFlag);
        if (!passError.equals("")) {
            errors.add(codeError);
        }

        return errors;
    }

    /*
     * 社員番号の入力チェックを行い、エラーメッセージを返却
     *
     * @return エラーメッセージ
     */
    private static String validateCode(EmployeeService service, String code, Boolean codeDuplicateCheckFlag) {
        // 入力値がなければエラーメッセージを返却
        if (code == null || code.equals("")) {
            return MessageConst.E_NOEMP_CODE.getMessage();
        }

        if (codeDuplicateCheckFlag) {
            // 社員番号の重複チェックを実施
            long employeesCount = isDuplicateEmployee(service, code);

            // 同一社員番号が既に登録されている場合はエラー
            if (employeesCount > 0) {
                return MessageConst.E_EMP_CODE_EXIST.getMessage();
            }
        }

        // エラーが無い場合は空文字を返す
        return "";
    }

    /*
     * 社員番号の重複チェック（同一データの件数を返却）
     */
    private static long isDuplicateEmployee(EmployeeService service, String code) {
        long employeesCount = service.countByCode(code);
        return employeesCount;
    }

    /*
     * 氏名の入力チェックを行い、エラーメッセージを返却
     *
     * @return エラーメッセージ
     */
    private static String validateName(EmployeeService service, String code) {
        // 入力値がなければエラーメッセージを返却
        if (code == null || code.equals("")) {
            return MessageConst.E_NONAME.getMessage();
        }

        // エラーが無い場合は空文字を返す
        return "";
    }

    /*
     * パスワードの入力チェックを行い、エラーメッセージを返却
     *
     * @return エラーメッセージ
     */
    private static String validatePassword(EmployeeService service, String code, Boolean passwordCheckFlag) {
        // 入力チェック実施指示あり、かつ入力値がなければエラーメッセージを返却
        if (passwordCheckFlag && (code == null || code.equals(""))) {
            return MessageConst.E_NOPASSWORD.getMessage();
        }

        // エラーが無い場合は空文字を返す
        return "";
    }
}

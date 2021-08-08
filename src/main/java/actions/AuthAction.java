package actions;

import java.io.IOException;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

/*
 * 認証に関わる処理を行うActionクラス
 */
public class AuthAction extends ActionBase {
    private EmployeeService service;

    @Override
    public void process() throws ServletException, IOException {
        service = new EmployeeService();
        invoke();
        service.close();
    }

    /*
     * ログイン画面を表示
     */
    public void showLogin() throws ServletException, IOException {

        // リクエストスコープにtokenを設定
        putRequestScope(AttributeConst.TOKEN, getTokenId());

        // セッションにフラッシュメッセージが登録されている場合、リクエストスコープに入れ替える
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, getSessionScope(AttributeConst.FLUSH));
            removeSessionScope(AttributeConst.FLUSH);
        }

        // ログイン情報を表示
        forward(ForwardConst.FW_LOGIN);
    }

    /*
     * ログイン処理を行う
     */
    public void login() throws ServletException, IOException {
        String code = getRequestParam(AttributeConst.EMP_CODE);
        String plainPass = getRequestParam(AttributeConst.EMP_PASS);
        String pepper = getContextScope(PropertyConst.PEPPER);

        // 有効な従業員か判断する(true:認証成功/false:認証失敗)
        Boolean isValidEmployee = service.validateLogin(code, plainPass, pepper);

        if (isValidEmployee) {
            // 認証成功の場合

            if (checkToken()) {
                // ログインした従業員のデータを取得
                EmployeeView ev = service.findOne(code, plainPass, pepper);
                // セッションにログインした従業員を設定
                putSessionScope(AttributeConst.LOGIN_EMP, ev);

                // ログイン完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGINED.getMessage());

                // トップページにリダイレクト
                redirect(ForwardConst.ACT_TOP, ForwardConst.CMD_INDEX);
            }
        } else {
            // 認証失敗の場合

            // リクエストスコープにtokenを設定
            putRequestScope(AttributeConst.TOKEN, getTokenId());
            // 認証失敗のフラグを立てる
            putRequestScope(AttributeConst.LOGIN_ERR, true);

            // ログイン画面を表示
            forward(ForwardConst.FW_LOGIN);
        }
    }

}

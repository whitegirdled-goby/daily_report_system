package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

public class EmployeeAction extends ActionBase {

    private EmployeeService service;

    @Override
    public void process() throws ServletException, IOException {
        service = new EmployeeService();
        invoke();

        service.close();
    }

    /*
     * 一覧画面を表示する
     */
    public void index() throws ServletException, IOException {
        // 指定されたページのデータを取得
        int page = getPage();

        List<EmployeeView> employees = service.getPerPage(page);

        // 全ての従業員データの件数を取得
        long employeeCount = service.countAll();

        putRequestScope(AttributeConst.EMPLOYEES, employees);
        putRequestScope(AttributeConst.EMP_COUNT, employeeCount);
        putRequestScope(AttributeConst.PAGE, page);
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE);

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_EMP_INDEX);
    }

    /*
     * 新規登録画面を表示する
     */
    public void entryNew() throws ServletException, IOException {
        putRequestScope(AttributeConst.TOKEN, getTokenId());
        putRequestScope(AttributeConst.EMPLOYEE, new EmployeeView());   // 空の従業員インスタンス

        // 新規登録画面に遷移
        forward(ForwardConst.FW_EMP_NEW);
    }

    /*
     * 新規登録を行う
     */
    public void create() throws ServletException, IOException {
        // CSRF対策tokenのチェック
        if (checkToken()) {
            // パラメータの値を元に従業員情報のインスタンスを作成
            EmployeeView ev = new EmployeeView(
                    null,
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue()
                    );

            // アプリケーションスコープからpepper文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            // 従業員情報登録
            List<String> errors = service.create(ev, pepper);

            if (errors.size() > 0) {
                // エラーがあった場合
                putRequestScope(AttributeConst.TOKEN, getTokenId());
                putRequestScope(AttributeConst.EMPLOYEE, ev);
                putRequestScope(AttributeConst.ERR, errors);

                // 新規登録画面を表示
                forward(ForwardConst.FW_EMP_NEW);

            } else {
                // エラーがなかった場合、登録完了のメッセージを表示する
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                // 一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }

    /*
     * 詳細画面を表示する
     */
    public void show() throws ServletException, IOException {

        // idを条件に従業員データを取得する
        EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

        if (ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {
            // データが取得できなかった、または論理削除されている場合エラー画面へ
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return;
        }

        // リクエストスコープに従業員データを設定
        putRequestScope(AttributeConst.EMPLOYEE, ev);

        // 詳細画面を表示
        forward(ForwardConst.FW_EMP_SHOW);
    }

    /*
     * 編集画面を表示する
     */
    public void edit() throws ServletException, IOException {

        // idを条件に従業員データを取得する
        EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

        if (ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {
            // データが取得できなかった、または論理削除されている場合エラー画面へ
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return;
        }

        // リクエストスコープにTOKENと従業員データを設定
        putRequestScope(AttributeConst.TOKEN, getTokenId());
        ev.setPassword("");
        putRequestScope(AttributeConst.EMPLOYEE, ev);

        // 編集画面を表示
        forward(ForwardConst.FW_EMP_EDIT);
    }

    /*
     * 更新を行う
     */
    public void update() throws ServletException, IOException {

        // tokenのチェック
        if (checkToken()) {
            // パラメータの値をもとに従業員情報のインスタンスを作成する
            EmployeeView ev = new EmployeeView(
                    toNumber(getRequestParam(AttributeConst.EMP_ID)),
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue()
                    );

            // アプリケーションスコープからPEPPER文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            // 従業員情報更新
            List<String> errors = service.update(ev, pepper);

            if (errors.size() > 0) {
                // 更新中にエラーが発生した場合
                putRequestScope(AttributeConst.TOKEN, getTokenId());
                putRequestScope(AttributeConst.EMPLOYEE, ev);
                putRequestScope(AttributeConst.ERR, errors);

                // 編集画面を再表示
                forward(ForwardConst.FW_EMP_EDIT);

            } else {
                // セッションに更新完了のフラッシュメッセージを設定
                putRequestScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                // 一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }

    /*
     * 論理削除を行う
     */
    public void destroy() throws ServletException, IOException {

        // tokenのチェック
        if (checkToken()) {
            // idを条件に従業員データを論理削除
            service.destroy(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            // セッションに削除完了のフラッシュメッセージを設定
            putRequestScope(AttributeConst.FLUSH, MessageConst.I_DELETED.getMessage());

            // 一覧画面にリダイレクト
            redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
        }
    }
}

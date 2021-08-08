package actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import constants.AttributeConst;
import constants.ForwardConst;
import constants.PropertyConst;

public abstract class ActionBase {
    protected ServletContext context;       // Webアプリケーションのコンテキスト情報
    protected HttpServletRequest request;   // リクエスト情報のオブジェクト
    protected HttpServletResponse response; // レスポンス情報のオブジェクト

    /*
     * 初期化処理
     */
    public void init(
            ServletContext servletContext,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        this.context = servletContext;
        this.request = servletRequest;
        this.response = servletResponse;
    }

    /*
     * フロントコントローラーから呼び出されるメソッド
     * 各サブクラスで内容を実装
     */
    public abstract void process() throws ServletException, IOException;

    /*
     * パラメータcommandの値に該当するメソッドを実行
     * commandの値が不正の場合はエラー画面を呼び出す
     */
    protected void invoke() throws ServletException, IOException {
        Method commandMethod;
        try {
            // パラメータからcommandを取得
            String command = request.getParameter(ForwardConst.CMD.getValue());

            // commandに該当するメソッドを実行
            commandMethod = this.getClass().getDeclaredMethod(command, new Class[0]);
            commandMethod.invoke(this, new Object[0]);  // 引数なし

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NullPointerException e) {
            e.printStackTrace();
            forward(ForwardConst.FW_ERR_UNKNOWN);
        }
    }

    /*
     * 引数で指定されたjspの呼び出し
     *
     * @param target 遷移先jsp画面のファイル名(拡張子を含まない)
     */
    protected void forward(ForwardConst target) throws ServletException, IOException {
        // jspファイルの相対パスを作成
        String forward = String.format("/WEB-INF/views/%s.jsp", target.getValue());
        RequestDispatcher dispatcher = request.getRequestDispatcher(forward);

        // jspファイルの呼び出し
        dispatcher.forward(request, response);
    }

    /*
     * 引数の値を元にURLを構築しリダイレクトを行う
     */
    protected void redirect(ForwardConst action, ForwardConst command) throws ServletException, IOException {
        // URLを構築
        String redirectUrl = request.getContextPath() + "/?action=" + action.getValue();
        if (command != null) {
            redirectUrl = redirectUrl + "&command=" + command.getValue();
        }

        // URLへリダイレクト
        response.sendRedirect(redirectUrl);
    }

    /*
     * CSRF対策 token不正の場合はエラー画面を表示
     * リクエストからパラメータtokenの値を取得し、セッションIDと比較
     * token不正の場合はエラー画面を表示
     *
     * @return true: token有効 false: token不正
     */
    protected boolean checkToken() throws ServletException, IOException {
        // パラメータからtokenの値を取得
        String _token = getRequestParam(AttributeConst.TOKEN);

        if (_token == null || !(_token.equals(getTokenId()))) {
            // tokenが設定されていない、またはセッションIDと一致しない場合エラー画面へ
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return false;
        }
        return true;
    }

    /*
     * リクエストからセッションIDを取得する
     *
     * @return セッションID
     */
    protected String getTokenId() {
        return request.getSession().getId();
    }

    /*
     * リクエストから表示を要求されているページ数を取得
     * 要求がない場合は1を返却
     *
     * @return 要求されている表示ページ数
     */
    protected int getPage() {
        int page;
        page = toNumber(request.getParameter(AttributeConst.PAGE.getValue()));
        if (page == Integer.MIN_VALUE) {
            page = 1;
        }
        return page;
    }

    /*
     * 文字列を数値に変換する
     *
     * @return 変換後数値
     */
    protected int toNumber(String strNumber) {
        int number = 0;
        try {
            number = Integer.parseInt(strNumber);
        } catch (Exception e) {
            number = Integer.MIN_VALUE;
        }
        return number;
    }

    /*
     * 文字列をLocalDate型に変換
     *
     * @return 返還後LocalDate型インスタンス
     */
    protected LocalDate toLocalDate(String strDate) {
        if (strDate == null || strDate.equals("")) {
            return LocalDate.now();
        }
        return LocalDate.parse(strDate);
    }

    /*
     * リクエストスコープから指定されたパラメータを取得、返却する
     *
     * @return パラメータの値
     */
    protected String getRequestParam(AttributeConst key) {
        return request.getParameter(key.getValue());
    }

    /*
     * リクエストスコープにパラメータを設定
     * 第2引数の型Vはジェネリクス（Generics・総称型）
     * すべての型を引数にとることができる
     */
    protected <V> void putRequestScope(AttributeConst key, V value) {
        request.setAttribute(key.getValue(), value);
    }

    /*
     * セッションスコープから指定されたパラメータを取得、返却する
     * セッションにはあらゆる型のオブジェクトを格納できるため、メソッドの戻り値はジェネリクスとしている
     *
     * @return パラメータの値
     */
    @SuppressWarnings("unchecked")
    protected <R> R getSessionScope(AttributeConst key) {
        return (R)request.getSession().getAttribute(key.getValue());
    }

    /*
     * セッションスコープにパラメータを設定
     */
    protected <V> void putSessionScope(AttributeConst key, V value) {
        request.getSession().setAttribute(key.getValue(), value);
    }

    /*
     * セッションスコープから指定された名前のパラメータを除去
     */
    protected void removeSessionScope(AttributeConst key) {
        request.getSession().removeAttribute(key.getValue());
    }

    /*
     * アプリケーションスコープから指定されたパラメータの値を取得、返却する
     *
     * @return パラメータの値
     */
    @SuppressWarnings("unchecked")
    protected <R> R getContextScope(PropertyConst key) {
        return (R)context.getAttribute(key.getValue());
    }
}

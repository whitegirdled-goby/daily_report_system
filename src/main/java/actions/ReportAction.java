package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import services.ReportService;

public class ReportAction extends ActionBase {

    private ReportService service;

    @Override
    public void process() throws ServletException, IOException {
        service = new ReportService();
        invoke();
        service.close();
    }

    /**
     * 一覧画面を表示する
     */
    public void index() throws ServletException, IOException {

        // 指定されたページ番号に表示する日報データを取得
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        // 日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS, reports);
        putRequestScope(AttributeConst.REP_COUNT, reportsCount);
        putRequestScope(AttributeConst.PAGE, page);
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE);

        // セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        // 一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }

    /*
     * 新規登録画面を表示する
     */
    public void entryNew() throws ServletException, IOException {
        // tokenをリクエストスコープに保管
        putRequestScope(AttributeConst.TOKEN, getTokenId());

        // 日報の空インスタンスを作成
        ReportView rv = new ReportView();

        // 本日日付をインスタンスの日報日付に設定しリクエストスコープに保管
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv);

        // 新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);
    }
}

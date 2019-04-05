package myamya.other.yajilin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.other.yajilin.YajilinSolver.Field;
import myamya.other.yajilin.YajilinSolver.Masu;
import net.arnx.jsonic.JSON;

/**
 * Servlet implementation class TestWeb
 */
@WebServlet("/YajilinWeb")
public class YajilinWeb extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public YajilinWeb() {
		super();
	}

	static class YajirinSolveThlead extends Thread {
		private final YajilinSolver solver;
		private String status = "時間切れです。途中経過を返します。";

		YajirinSolveThlead(List<String> args) {
			solver = new YajilinSolver(args);
		}

		YajirinSolveThlead(int height, int width, String param) {
			solver = new YajilinSolver(height, width, param);
		}

		public void run() {
			status = solver.solve();
		}

		public Field getResult() {
			return solver.getField();
		}

		public String getStatus() {
			return status;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<String> parts = Arrays.asList(request.getParameter("url").split("/"));
			int height = Integer.parseInt(parts.get(parts.size() - 2));
			int width = Integer.parseInt(parts.get(parts.size() - 3));
			if (height * width > 400) {
				resultMap.put("result", "");
				resultMap.put("status", "申し訳ございません。401マス以上ある問題は解くことができません。");
			} else {
				YajirinSolveThlead t = new YajirinSolveThlead(height, width,
						parts.get(parts.size() - 1));
				//			List<String> args = Arrays.asList(request.getParameter("args").split("\n"));
				//			YajirinSolveThlead t = new YajirinSolveThlead(args);

				t.start();
				t.join(27000);
				resultMap.put("result", makeCambus(t.getResult()));
				resultMap.put("status", t.getStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", "");
			resultMap.put("status", "エラーが発生しました。urlを確認してください。短縮URLは認識できない場合があります。");
		}
		try (PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

	/**
	 * キャンバスHTMLの作成
	 */
	private String makeCambus(Field field) {
		int baseSize = 20;
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<svg xmlns=\"http://www.w3.org/2000/svg\" "
						+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
						+ (field.getXLength() * baseSize + baseSize) + "\" >");
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				Masu oneMasu = field.getMasu()[yIndex][xIndex];
				sb.append("<text y=\"" + (yIndex * baseSize + baseSize)
						+ "\" x=\""
						+ (xIndex * baseSize + baseSize)
						+ "\" font-size=\""
						+ (baseSize)
						+ "\" textLength=\""
						+ (baseSize)
						+ "\" lengthAdjust=\"spacingAndGlyphs\">"
						+ oneMasu.toStringForweb()
						+ "</text>");
			}
		}
		sb.append("</svg>");
		return sb.toString();
	}
}
package myamya.other.solver;

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

import myamya.other.solver.nurikabe.NurikabeSolver;
import myamya.other.solver.yajilin.YajilinSolver;
import net.arnx.jsonic.JSON;

/**
 * Servlet implementation class TestWeb
 */
@WebServlet("/SolverWeb")
public class SolverWeb extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public SolverWeb() {
		super();
	}

	static abstract class AbsSolveThlead extends Thread {
		protected final Solver solver;
		private String status = "時間切れです。途中経過を返します。";

		AbsSolveThlead(int height, int width, String param) {
			solver = getSolver(height, width, param);
		}

		abstract protected Solver getSolver(int height, int width, String param);

		public void run() {
			status = solver.solve();
		}

		public String getStatus() {
			return status;
		}
	}

	static class YajirinSolveThlead extends AbsSolveThlead {
		YajirinSolveThlead(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new YajilinSolver(height, width, param);
		}

		public String makeCambus() {
			YajilinSolver.Field field = ((YajilinSolver) solver).getField();
			int baseSize = 20;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					YajilinSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
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

	static class NurikabeSolverThread extends AbsSolveThlead {
		NurikabeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new NurikabeSolver(height, width, param);
		}

		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NurikabeSolver.Field field = ((NurikabeSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					NurikabeSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
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
			String puzzleType = parts.get(parts.size() - 4);
			if (puzzleType.contains("yajilin")) {
				if (height * width > 400) {
					resultMap.put("result", "");
					resultMap.put("status", "申し訳ございません。401マス以上ある問題は解くことができません。");
				} else {
					YajirinSolveThlead t = new YajirinSolveThlead(height, width,
							parts.get(parts.size() - 1));
					t.start();
					t.join(28000);
					resultMap.put("result", t.makeCambus());
					resultMap.put("status", t.getStatus());
				}
			} else if (puzzleType.contains("nurikabe")) {
				NurikabeSolverThread t = new NurikabeSolverThread(height, width,
						parts.get(parts.size() - 1));
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else {
				throw new IllegalArgumentException();
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
}
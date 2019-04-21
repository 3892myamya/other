package myamya.other.hitandblow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class HitAndBlow {

	/**
	 * 数値リストのラッパー
	 */
	static class Numbers implements Iterable<Integer> {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((numbers == null) ? 0 : numbers.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Numbers other = (Numbers) obj;
			if (numbers == null) {
				if (other.numbers != null)
					return false;
			} else if (!numbers.equals(other.numbers))
				return false;
			return true;
		}

		private final List<Integer> numbers;

		public Numbers(List<Integer> numbers) {
			this.numbers = numbers;
		}

		public Numbers(Numbers other) {
			this.numbers = new ArrayList<>(other.numbers);
		}

		/**
		 * 自身に対し、候補の数値を聞いて応答を返す
		 */
		int check(Numbers cand) {
			int result = 0;
			for (int i = 0; i < cand.size(); i++) {
				int num = cand.get(i);
				if (numbers.get(i) == num) {
					result = result + 10;
				} else if (numbers.contains(num)) {
					result++;
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return numbers.toString();
		}

		public int get(int i) {
			return numbers.get(i);
		}

		public boolean contains(int num) {
			return numbers.contains(num);
		}

		public int size() {
			return numbers.size();
		}

		@Override
		public Iterator<Integer> iterator() {
			return numbers.iterator();
		}

	}

	/**
	 * 回答者
	 */
	interface Solver {
		public default String getName() {
			return this.getClass().getSimpleName();
		}

		public Numbers makeCand();

		public default void addResult(Numbers cand, int response) {
			//
		}

		public default int check(Numbers cand) {
			return getAnswer().check(cand);
		}

		public Numbers getAnswer();

	}

	static abstract class AbsSolver implements Solver {
		private final int length;
		private final List<Numbers> candList = new ArrayList<>();

		AbsSolver(int length, int numCnt, boolean permitDupl) {
			this.length = length;
			List<List<Integer>> candNumsList = new ArrayList<>();
			List<Integer> numList = new ArrayList<>();
			for (int i = 0; i < numCnt; i++) {
				numList.add((i + 1) % 10);
			}
			permutation(candNumsList, numList, length, permitDupl,
					new ArrayList<>());
			for (List<Integer> candNums : candNumsList) {
				candList.add(new Numbers(candNums));
			}
		}

		/**
		 * candに含まれる候補の順列をlengthの長さだけ詰めた一覧を作る。
		 * 重複を許す場合はpermitDuplをtrueにする。
		 * @param <T>
		 */
		private static <T> void permutation(List<List<T>> result,
				List<T> cand, int length, boolean permitDupl,
				List<T> ans) {
			if (length == 0) {
				result.add(new ArrayList<>(ans));
			} else {
				for (int i = 0; i < cand.size(); i++) {
					if (permitDupl) {
						ans.add(cand.get(i));
					} else {
						ans.add(cand.remove(i));
					}
					permutation(result, cand, length -
							1, permitDupl, ans);
					if (permitDupl) {
						ans.remove(ans.size() - 1);
					} else {
						cand.add(i,
								ans.remove(ans.size() - 1));
					}
				}
			}
		}

		protected int getLength() {
			return length;
		}

		@Override
		public Numbers makeCand() {
			return candList.get(new Random().nextInt(candList.size()));
		}

		protected final List<Numbers> getCandList() {
			return candList;
		}

	}

	//	/**
	//	 * ダメだった奴だけ除外し、あとはランダムで
	//   * →やっぱいらないか
	//	 */
	//	static class ExceptSolver extends AbsSolver {
	//		ExceptSolver(int length, boolean withoutZero, boolean permitDupl) {
	//			super(length, withoutZero, permitDupl);
	//		}
	//
	//		@Override
	//		public void addResult(Numbers cand, int response) {
	//			getCandList().remove(cand);
	//		}
	//	}

	/**
	 * 候補を絞っていくソルバー
	 * レベルは0-4まで。4が一番強い
	 */
	static class CandSolver extends AbsSolver {
		private final Numbers answer;
		private final int level;

		CandSolver(int length, int numCnt, boolean permitDupl, int level) {
			super(length, numCnt, permitDupl);
			this.answer = new Numbers(getCandList().get(new Random().nextInt(getCandList().size())));
			this.level = level;
		}

		@Override
		public void addResult(Numbers cand, int response) {
			// 自分自身は必ず消す
			getCandList().remove(cand);
			// 自分と同じ見え方にならない奴は候補から消す。
			for (Iterator<Numbers> iterator = getCandList().iterator(); iterator.hasNext();) {
				int virtualResponse = iterator.next().check(cand);
				if (response != virtualResponse) {
					if (level > new Random().nextInt(4))
						iterator.remove();
				}
			}
		}

		@Override
		public Numbers getAnswer() {
			return answer;
		}

	}

	//	/**
	//	 * より強くなったCandSolver
	//   * 計算時間がちょっと長くなるので封印
	//	 */
	//	static class CandSolverStrong extends CandSolver {
	//
	//		private final List<Numbers> allCandList;
	//
	//		CandSolverStrong(int length, boolean withoutZero, boolean permitDupl) {
	//			super(length, withoutZero, permitDupl);
	//			allCandList = new ArrayList<>(getCandList());
	//		}
	//
	//		@Override
	//		public Numbers makeCand() {
	//			//if (allCandList.size() == getCandList().size()) {
	//			//	// TODO これを入れると厳密には重複を許すルールの時に弱くなるが速度が遅くなりすぎるので致し方なし
	//			//	return allCandList.get(new Random().nextInt(allCandList.size()));
	//			//}
	//			List<Numbers> candList = null;
	//			int mixExpected = Integer.MAX_VALUE;
	//			boolean requireIncludeCollect = false;
	//
	//			for (Numbers oneCand : allCandList) {
	//				// 質問に対する候補が返す値を hit - blow ごとに集計
	//				Map<Integer, Integer> wkMap = new HashMap<>();
	//				for (Numbers retain : getCandList()) {
	//					int key = retain.check(oneCand);
	//					Integer cnt = wkMap.get(key);
	//					if (cnt == null) {
	//						cnt = 0;
	//					}
	//					wkMap.put(key, cnt + 1);
	//				}
	//				int expected = 0;
	//				for (int cnt : wkMap.values()) {
	//					expected = expected + (cnt * cnt);
	//				}
	//				// 正解が含まれる候補を優先する
	//				boolean includeCollect = wkMap.containsKey(getLength() * 10);
	//				if (expected < mixExpected) {
	//					candList = new ArrayList<>();
	//					mixExpected = expected;
	//					requireIncludeCollect = includeCollect;
	//				}
	//				if (expected == mixExpected) {
	//					if (includeCollect &&
	//							!requireIncludeCollect) {
	//						candList = new ArrayList<>();
	//						requireIncludeCollect = true;
	//					}
	//				}
	//				if (expected == mixExpected &&
	//						includeCollect == requireIncludeCollect) {
	//					candList.add(oneCand);
	//				}
	//			}
	//			return candList.get(new Random().nextInt(candList.size()));
	//		}
	//
	//		protected final List<Numbers> getAllCandList() {
	//			return allCandList;
	//		}
	//	}

	/**
	 * ずるしてさらに強くなったソルバー
	 */
	static class Zool extends CandSolver {

		private List<Numbers> answerCand;

		Zool(int length, int numCnt, boolean permitDupl) {
			super(length, numCnt, permitDupl, 4);
			answerCand = new ArrayList<>(getCandList());
		}

		@Override
		public int check(Numbers cand) {
			// 質問に対する候補が返す値を hit - blow ごとに集計
			Map<Integer, List<Numbers>> answerCandMap = new HashMap<>();
			Map<Integer, Integer> wkMap = new HashMap<>();
			for (Numbers oneCand : answerCand) {
				int key = oneCand.check(cand);
				Integer cnt = wkMap.get(key);
				if (cnt == null) {
					cnt = 0;
				}
				wkMap.put(key, cnt + 1);
				List<Numbers> answerCandList = answerCandMap.get(key);
				if (answerCandList == null) {
					answerCandList = new ArrayList<>();
					answerCandMap.put(key,
							answerCandList);
				}
				answerCandList.add(oneCand);
			}
			// もっとも候補数が多いものを返す
			List<Integer> useKeyCandList = null;
			int maxExpected = Integer.MIN_VALUE;
			boolean requireNotIncludeCollect = false;
			for (Entry<Integer, Integer> e : wkMap.entrySet()) {
				// 正解が含まれない候補を優先する
				int expected = e.getValue();
				boolean notIncludeCollect = (e.getKey() != (getLength() * 10));
				if (expected > maxExpected) {
					useKeyCandList = new ArrayList<>();
					maxExpected = expected;
					requireNotIncludeCollect = notIncludeCollect;
				}
				if (expected == maxExpected) {
					if (notIncludeCollect &&
							!requireNotIncludeCollect) {
						useKeyCandList = new ArrayList<>();
						requireNotIncludeCollect = true;
					}
				}
				if (expected == maxExpected &&
						notIncludeCollect == requireNotIncludeCollect) {
					useKeyCandList.add(e.getKey());
				}
			}
			answerCand = answerCandMap.get(useKeyCandList.get(new Random().nextInt(useKeyCandList.size())));
			return super.check(cand);
		}

		@Override
		public Numbers getAnswer() {
			return answerCand.get(new Random().nextInt(answerCand.size()));
		}
	}

	// プレイヤー
	static class Human extends AbsSolver {
		private final Numbers answer;

		Human(int length, int numCnt, boolean permitDupl) {
			super(length, numCnt, permitDupl);
			Numbers answer = null;
			Scanner scanner = new Scanner(System.in);
			while (answer == null) {
				System.out.println("あなたの数字を入力してください。");
				String input = scanner.nextLine();
				answer = makeNumbers(input);
				if (answer == null) {
					System.out.print("入力が誤っています。もう一度");
				}
			}
			System.out.println("あなたの数字は" + answer + "に決まりました。");
			this.answer = answer;
		}

		private Numbers makeNumbers(String input) {
			if (input.length() != getLength()) {
				return null;
			}
			List<Integer> numList = new ArrayList<>();
			for (int i = 0; i < getLength(); i++) {
				try {
					numList.add(Integer.parseInt(input.substring(i, i + 1)));
				} catch (NumberFormatException e) {
					return null;
				}
			}
			Numbers number = new Numbers(numList);
			if (!getCandList().contains(number)) {
				return null;
			}
			return number;
		}

		@Override
		public Numbers makeCand() {
			Numbers cand = null;
			Scanner scanner = new Scanner(System.in);
			while (cand == null) {
				System.out.println("相手に聞く数字を入力してください。");
				String input = scanner.nextLine();
				cand = makeNumbers(input);
				if (cand == null) {
					System.out.print("入力が誤っています。もう一度");
				}
			}
			return cand;
		}

		@Override
		public Numbers getAnswer() {
			return answer;
		}

	}

	public static void main(String[] args) throws InterruptedException {
		long stime = System.nanoTime();
		// 戦績に関する情報
		int play = 100;
		int allcnt = 0;
		int solved1win = 0;
		int solved2win = 0;
		int draw = 0;
		// ルールに関する情報
		int enemyLevel = 5;
		int numCnt = 5;
		int length = 2;
		boolean permitDupl = false;
		for (int i = 0; i < play; i++) {
			Solver solver1 = new Zool(length, numCnt, permitDupl);
			//Solver solver1 = new Human(length, numCnt, permitDupl);
			//Solver solver2 = new Zool(length, numCnt, permitDupl);
			Solver solver2 = new CandSolver(length, numCnt, permitDupl, enemyLevel);
			int cnt = 0;
			boolean solved1 = false;
			boolean solved2 = false;
			while (!solved1 && !solved2) {
				cnt++;
				solved1 = play(length, solver1, solver2);
				solved2 = play(length, solver2, solver1);
			}
			if (solved1 && solved2) {
				System.out.println(cnt + "回で同時にクリアしました");
				draw++;
			} else if (solved1) {
				System.out.println(cnt + "回で" +
						solver1.getName() + "がクリアしました");
				solved1win++;
			} else if (solved2) {
				System.out.println(cnt + "回で" +
						solver2.getName() + "がクリアしました");
				solved2win++;
			}
			System.out.println(solver1.getName() + "の答え >=" + solver1.getAnswer());
			System.out.println(solver2.getName() + "の答え >=" + solver2.getAnswer());
			allcnt = allcnt + cnt;
			System.out.println(play + "回までの合計調査数:" + allcnt);
			System.out.println(solver1.getName() + "の" +
					solved1win + "勝" + solved2win + "敗" + draw + "分");
		}
		System.out.println("消費時間 = " + (System.nanoTime() -
				stime) / 1000000 + "msec.");
	}

	private static boolean play(int length, Solver self, Solver enemy) {
		Numbers cand = self.makeCand();
		int response = enemy.check(cand);
		System.out.println(
				self.getName() + " の結果 " + cand + " => [hit=" + response / 10 + ", blow=" + response % 10 + "]");
		if (response == length * 10) {
			return true;
		} else {
			self.addResult(cand, response);
			return false;
		}
	}

}
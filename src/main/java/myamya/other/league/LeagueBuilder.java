package myamya.other.league;

import java.util.ArrayList;
import java.util.List;

import lombok.Value;

public class LeagueBuilder {

	/**
	 * リーグ戦の組み合わせのペアです。
	 */
	@Value
	public static class IndexPair {

		private final int teamIndexX;
		private final int teamIndexY;

		@Override
		public String toString() {
			return teamIndexX + "-" + teamIndexY;
		}

		/**
		 * 自身とotherが同じ組み合わせかどうかを返します。
		 */
		public boolean isSame(IndexPair other) {
			return ((teamIndexX == other.teamIndexX && teamIndexY == other.teamIndexY) ||
					(teamIndexX == other.teamIndexY && teamIndexY == other.teamIndexX));
		}

	}

	/**
	 * リーグ戦の組み合わせを生成します。
	 */
	public static List<List<IndexPair>> getIndexPairListList(int playerCount) {
		List<List<IndexPair>> result = new ArrayList<>();
		for (int i = 0; i < playerCount - 1 + (playerCount % 2); i++) {
			result.add(getIndexPairList(i, playerCount));
		}
		return result;
	}

	/**
	 * リーグ戦の1回戦の組み合わせを生成します。
	 */
	private static List<IndexPair> getIndexPairList(int match, int playerCount) {
		List<IndexPair> result = new ArrayList<>();
		int maxIndex = playerCount - 1;
		int adjust = playerCount % 2;
		int adjustVal = maxIndex + adjust;
		for (int i = 0; i < adjustVal / 2; i++) {
			int candMe = match - i - 1;
			if (candMe < 0) {
				candMe = candMe + adjustVal;
			}
			int candEnemy = match + i + 1;
			if (candEnemy > adjustVal - 1) {
				candEnemy = candEnemy - adjustVal;
			}
			result.add(new IndexPair(candMe, candEnemy));
		}
		if (adjust == 0) {
			result.add(new IndexPair(match, maxIndex));
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(getIndexPairListList(Integer.parseInt(args[0])));
	}
}

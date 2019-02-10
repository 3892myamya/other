package myamya.other.league;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import myamya.other.league.LeagueBuilder.IndexPair;

public class LeagueBuilderTest {

	/**
	 * getIndexPairListListのテスト
	 */
	@Test
	public void getIndexPairListListTest_001() throws IOException {
		// 三か
		for (int i = 1; i <= 30; i++) {
			List<List<IndexPair>> league = LeagueBuilder.getIndexPairListList(i);
			System.out.println("選手数が" + i + "の場合の組み合わせ全部" + league);
			// リーグ戦の対戦回数は、選手数が偶数なら選手数 - 1、選手数が奇数なら選手数に等しいこと
			assertEquals(league.size(), i - 1 + (i % 2));
			Set<IndexPair> testIndexPair = new HashSet<>();
			for (List<IndexPair> oneBattle : league) {
				// 1回の対戦の件数は、参加選手数を2で割った値(小数点以下切り捨て)であること
				assertEquals(oneBattle.size(), i / 2);
				Set<Integer> testSet = new HashSet<>();
				for (IndexPair oneMatch : oneBattle) {
					// 同じ選手同士が戦っていないこと
					assertNotEquals(oneMatch.getTeamIndexX(), oneMatch.getTeamIndexY());
					// 同じ選手が複数回登場していないこと
					assertEquals(testSet.contains(oneMatch.getTeamIndexX()), false);
					testSet.add(oneMatch.getTeamIndexX());
					assertEquals(testSet.contains(oneMatch.getTeamIndexY()), false);
					testSet.add(oneMatch.getTeamIndexY());
					// 同じ組み合わせが他の日に行われていないこと
					for (IndexPair already : testIndexPair) {
						assertEquals(already.isSame(oneMatch), false);
					}
					testIndexPair.add(oneMatch);
				}
			}
		}
	}

}

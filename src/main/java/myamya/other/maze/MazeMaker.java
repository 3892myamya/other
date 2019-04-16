package myamya.other.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MazeMaker {
	static class Position {

		@Override
		public String toString() {
			return "Position [yIndex=" + yIndex + ", xIndex=" +
					xIndex + "]";
		}

		private final int yIndex;
		private final int xIndex;

		public Position(int yIndex, int xIndex) {
			this.yIndex = yIndex;
			this.xIndex = xIndex;
		}

		public int getyIndex() {
			return yIndex;
		}

		public int getxIndex() {
			return xIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + xIndex;
			result = prime * result + yIndex;
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
			Position other = (Position) obj;
			if (xIndex != other.xIndex)
				return false;
			if (yIndex != other.yIndex)
				return false;
			return true;
		}

	}

	static class Maze {
		private final String[][] maze;

		Maze(int height, int width) {
			maze = new String[height * 2 + 1][width * 2 + 1];
			// 初期配置
			List<Position> brokenWallList = new ArrayList<>();
			Map<Position, Position> group = new LinkedHashMap<>();
			for (int y = 0; y < maze.length; y++) {
				for (int x = 0; x < maze[y].length; x++) {
					if (y % 2 == 1 && x % 2 == 1) {
						maze[y][x] = "　";
						Position pos = new Position(y, x);
						group.put(pos, pos);
					} else {
						maze[y][x] = "■";
						if ((y != 0 && x != 0 && y != maze.length - 1 && x != maze[y].length - 1)
								&& (y % 2

										+ x % 2 == 1)) {

							brokenWallList.add(new Position(y, x));
						}
					}
				}
			}
			// 迷路生成アルゴリズムはクラスタリング法を採用
			Collections.shuffle(brokenWallList);
			for (Position pos : brokenWallList) {
				Position masuA;
				Position masuB;
				if (pos.getyIndex() % 2 == 0) {
					masuA = new Position(pos.getyIndex()
							- 1, pos.getxIndex());
					masuB = new Position(pos.getyIndex()
							+ 1, pos.getxIndex());
				} else {
					masuA = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					masuB = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				}
				Position aGroupLeader = getGroupLeader(group, masuA);
				Position bGroupLeader = getGroupLeader(group, masuB);
				if (aGroupLeader.equals(bGroupLeader)) {
					continue;
				} else {

					maze[pos.getyIndex()][pos.getxIndex()] = "　";
					group.put(bGroupLeader,
							aGroupLeader);
				}
			}
		}

		/**
		         * あるマスのリーダーのマスを調べる
		         */
		private Position getGroupLeader(Map<Position, Position> group, Position masu) {
			Position target = masu;
			Position result = null;
			Set<Position> childrens = new HashSet<>();
			while (result == null) {
				Position parent = group.get(target);
				if (target.equals(parent)) {
					result = parent;
				} else {
					childrens.add(parent);
					target = parent;
				}
			}
			// 子分を全て新しいリーダーに付け替える(Union-Find木をならす処理)
			for (Position children : childrens) {
				group.put(children, result);
			}

			return result;
		}

		void setStartAndGoal() {
			// スタートとゴール地点決定
			List<Position> candStartList = new ArrayList<>();
			for (int y = 0; y < maze.length; y++) {
				for (int x = 0; x < maze[y].length; x++) {
					if (maze[y][x].equals("　")) {
						candStartList.add(new Position(y, x));
					}
				}
			}
			Position randomPos = candStartList.get(new Random().nextInt(candStartList.size()));
			Position StartPos = getMostFarPosition(randomPos);
			Position EndPos = getMostFarPosition(StartPos);
			maze[StartPos.getyIndex()][StartPos.getxIndex()] = "Ｓ";
			maze[EndPos.getyIndex()][EndPos.getxIndex()] = "Ｇ";
		}

		// ある地点から最も遠い地点を調べる
		private Position getMostFarPosition(Position firstPos) {
			Map<Integer, List<Position>> distanceMap = new LinkedHashMap<>();
			Set<Position> resolvePosSet = new HashSet<>();
			List<Position> nextPosList = new ArrayList<>();
			int distance = 0;
			nextPosList.add(firstPos);
			distanceMap.put(distance, nextPosList);
			resolvePosSet.addAll(nextPosList);
			while (true) {
				nextPosList = new ArrayList<>();
				List<Position> nowPosList = distanceMap.get(distance);
				for (Position nowPos : nowPosList) {
					Position candPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
					if (maze[candPos.getyIndex()][candPos.getxIndex()].equals("　")
							&&
							!resolvePosSet.contains(candPos)) {
						nextPosList.add(candPos);
						resolvePosSet.add(candPos);
					}
					candPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
					if (maze[candPos.getyIndex()][candPos.getxIndex()].equals("　")
							&&
							!resolvePosSet.contains(candPos)) {
						nextPosList.add(candPos);
						resolvePosSet.add(candPos);
					}
					candPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
					if (maze[candPos.getyIndex()][candPos.getxIndex()].equals("　")
							&&
							!resolvePosSet.contains(candPos)) {
						nextPosList.add(candPos);
						resolvePosSet.add(candPos);
					}
					candPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
					if (maze[candPos.getyIndex()][candPos.getxIndex()].equals("　")
							&&
							!resolvePosSet.contains(candPos)) {
						nextPosList.add(candPos);
						resolvePosSet.add(candPos);
					}
				}
				if (nextPosList.isEmpty()) {
					break;
				} else {
					distanceMap.put(++distance,
							nextPosList);
				}
			}
			return distanceMap.get(distance).get(new Random().nextInt(distanceMap.get(distance).size()));
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int y = 0; y < maze.length; y++) {
				for (int x = 0; x < maze[y].length; x++) {
					if (maze[y][x].equals("■")) {
						boolean upExist = (y != 0 &&
								maze[y - 1][x].equals("■"));
						boolean rightExist = (x != maze[y].length - 1 && maze[y][x + 1].equals("■"));
						boolean downExist = (y != maze.length - 1 && maze[y + 1][x].equals("■"));
						boolean leftExist = (x != 0
								&& maze[y][x - 1].equals("■"));
						int bit = (upExist ? 8 : 0)
								+ (rightExist ? 4 : 0) + (downExist ? 2 : 0) + (leftExist ? 1 : 0);
						switch (bit) {
						case 1:
							sb.append("─");
							break;
						case 2:
							sb.append("│");
							break;
						case 3:
							sb.append("┐");
							break;
						case 4:
							sb.append("─");
							break;
						case 5:
							sb.append("─");
							break;
						case 6:
							sb.append("┌");
							break;
						case 7:
							sb.append("┬");
							break;
						case 8:
							sb.append("│");
							break;
						case 9:
							sb.append("┘");
							break;
						case 10:
							sb.append("│");
							break;
						case 11:
							sb.append("┤");
							break;
						case 12:
							sb.append("└");
							break;
						case 13:
							sb.append("┴");
							break;
						case 14:
							sb.append("├");
							break;
						case 15:
							sb.append("┼");
							break;
						default:
							break;
						}
					} else {
						sb.append(maze[y][x]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}
	}

	/**
	 * 迷路を作ります。
	 * 引数は縦の長さ・横の長さです。
	 */
	public static void main(String[] args) throws InterruptedException {
		//while (true) {
			long s = System.nanoTime();
			int height = Integer.parseInt(args[0]);
			int width = Integer.parseInt(args[1]);
			Maze maze = new Maze(height, width);
			maze.setStartAndGoal();
			System.out.println(maze.toString());
			System.out.println((System.nanoTime() - s) / 1000000 +
					"msec.");
		//	Thread.sleep(1500);
		//}
	}

}

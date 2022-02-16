package homework.data;

import java.util.HashMap;

public interface GameService {
	
	public Game[] getAllGames();
	
	public Game getGame(String id);
	
	public Game startGame(Game g);
	
	public void deleteGame(String game_id);
	
	public HashMap<Boolean, String> makeAMove(String game_id, String new_board);
}

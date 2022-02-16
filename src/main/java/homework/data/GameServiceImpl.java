package homework.data;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Service;

import homework.data.Game.StatusString;

@Service
public class GameServiceImpl implements GameService{
	
	private static HashMap<String, Game> games = new HashMap<>();
	
	@Override
	public Game[] getAllGames()
	{			
		Game[] game_list = new Game[games.values().size()];
		int i = 0;		
		
		for(Game g: games.values())
		{
			game_list[i] = g;
			++i;
		}
		
		return game_list;
	}
	
	@Override
	public Game getGame(String id)
	{
		return games.get(id);
	}
	
	@Override
	public Game startGame(Game g)
	{
		// Use the input game to create a new
		// game on the server. Use only the first move
		// from the parameters.
		Game actual_game = new Game(g.getBoard());
				
		games.put(actual_game.getId(), actual_game);
				
		return actual_game;
	}
	
	@Override
	public void deleteGame(String game_id)
	{
		if(games.containsKey(game_id))
		{
			games.remove(game_id);
		}
	}
	
	
	// This function should validate the move using validateMove
	// before commiting it to the actual game
	@Override
	public HashMap<Boolean, String> makeAMove(String game_id, String new_board)
	{
		HashMap<Boolean, String> response = new HashMap<>();
		
		Game target_game = games.get(game_id);
		
		if(target_game == null)
		{
			response.put(Boolean.FALSE, "Game not found!");
			return response;
		}
		
		if(target_game.getStatus() != StatusString.RUNNING)
		{
			response.put(Boolean.FALSE, "Game is over!");
			return response;
		}		
		
		if(target_game.validateMove(new_board) == false)
		{
			response.put(Boolean.FALSE, "Invalid move!");
			return response;
		}
		
		target_game = target_game.makeMove(new_board);
		
		response.put(Boolean.TRUE, "Move ok");
		
		return response;
	}
	
}


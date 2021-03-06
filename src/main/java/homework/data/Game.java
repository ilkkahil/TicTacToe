package homework.data;

import java.util.ArrayList;

public class Game {
	// The game's UUID, read-only, generated by the server. The client can not POST or PUT this.
	private String id;
	// The board state
	private String board;
	// The game status, read-only, the client can not POST or PUT this
	private StatusString status;
	
	public enum StatusString
	{
		RUNNING("The game is in progress"),
		X_WON("The winner is X"),
		O_WON("The winner is O"),
		DRAW("The game is a draw");
		
		private String description;
		
		StatusString(String desc)
		{
			this.description = desc;
		}
		
		public String getDescription()
		{
			return this.description;
		}		
	}
	
	public Game() {
		super();
	}
	
	public Game(String start_board)
	{
		super();
				
		// If no move is made "AI" makes the optimal move
		// taking one of the corners
		if(start_board == null || start_board.length() == 0 || (!start_board.contains("X") && !start_board.contains("O")))
		{
			this.board = "X--------";
		}
		else
		{
			this.board = start_board;
			makeCounterMove();
		}
		
		this.status = StatusString.RUNNING;
		
		// Use time as the game id
		// This won't work if there are multiple games created on the same millisecond
		this.id = String.valueOf(System.currentTimeMillis());
	}		
	
	// Checks if the new board state is valid
	// This needs to be called before makeMove
	public boolean validateMove(String new_board)
	{
		boolean move_found = false;
		int x_count = 0, o_count = 0;
		int current_x = 0, current_o = 0;
		
		if(new_board.length() != board.length())
		{
			return false;
		}
		
		for(int i = 0; i < board.length(); ++i)
		{			
			if(new_board.charAt(i) == 'O')
			{
				++o_count;
			}
			else if (new_board.charAt(i) == 'X') 
			{
				++x_count;
			}
			
			if(this.board.charAt(i) == 'O')
			{
				++current_o;
			}else if(this.board.charAt(i) == 'X')
			{
				++current_x;
			}			
			
			// Check if the current and future boards have differences
			// If more than one is found, the new board is invalid
			boolean match = (board.charAt(i) == new_board.charAt(i));
			if(!match)
			{
				if(move_found)
				{
					return false;
				}
				move_found = true;
			}
		}
		
		// No move made
		if( current_x == x_count && current_o == o_count )
		{
			return false;
		}
		// Something removed
		if(current_x > x_count || current_o > o_count)
		{
			return false;
		}

		// Too many moves
		if( Math.abs(o_count - x_count) > 1)
		{
			return false;
		}
		
		return move_found;
	}
	
	// Commits a move from the player and
	// one from the "AI" if possible
	// Validity of the move needs to be checked
	// before with validateMove
	public Game makeMove(String new_board)
	{
		this.board = new_board;
		
		if(!moves_left(this.board))
		{
			// The player's move ends the game
			int result = evaluate(this.board, 'X');
			
			if(result < 0)
			{
				this.status = StatusString.O_WON;
			}else if(result > 0)
			{
				this.status = StatusString.X_WON;
			}else
			{
				this.status = StatusString.DRAW;
			}
			return this;
		}
		
		// "AI"'s response to player move
		makeCounterMove();
			
		if(!moves_left(this.board))
		{
			// "AI"'s move ends the game
			int result = evaluate(this.board, 'X');
			
			if(result < 0)
			{
				this.status = StatusString.O_WON;
			}else if(result > 0)
			{
				this.status = StatusString.X_WON;
			}else
			{
				this.status = StatusString.DRAW;		
			}
		}
		
		return this;
	}
	
	// Makes "AI"'s move
	// Is called only if there are moves left
	public void makeCounterMove()
	{
		int x_count = 0, o_count = 0;
		int best_move = -1000, temp_val, best_move_index = 0;
		char[] char_board;
		char ai_char = 'X';
		ArrayList<Integer> possible_moves = new ArrayList<>();
		
		// Go over the gameboard once to figure out what 
		// symbol the AI should use and what moves are available
		for(int i = 0; i < board.length(); ++i)
		{
			if(board.charAt(i) == 'X' || board.charAt(i) == 'x')
			{
				++x_count;
			}else if(board.charAt(i) == 'O' || board.charAt(i) == 'O')
			{
				++o_count;
			}else
			{
				possible_moves.add(i);
			}
		}
		
		// Try to find the best move using minMax
		// function on the open positions
		for(Integer it : possible_moves)
		{
			char_board = this.board.toCharArray();
			
			if(x_count <= o_count)
			{
				 ai_char = 'X';
			}else
			{
				ai_char = 'O';
			}
			
			char_board[it] = ai_char;
			String temp_board = new String(char_board);			 
			
			temp_val = minMax(temp_board, 0, ai_char, false);
						
			if(temp_val > best_move)
			{
				best_move = temp_val;
				best_move_index = it;
			}	 
		}
		
		char_board = this.board.toCharArray();
		
		if(x_count <= o_count)
		{
			char_board[best_move_index] = 'X';
		}
		else
		{
			char_board[best_move_index] = 'O';
		}
		
		this.board = new String(char_board);
	}
	
	
	// function that SHOULD return the best move adjusted
	// Based on https://www.geeksforgeeks.org/minimax-algorithm-in-game-theory-set-3-tic-tac-toe-ai-finding-optimal-move/
	
	// Returns values describing the gamestate in the future
	// these values are adjusted with the depth of recursion
	// in order to find the fastest win.
	public int minMax(String current_board, int depth, char ai_char, boolean ai_turn)
	{
		int best_move;
		
		if(!moves_left(current_board))
		{
			return evaluate(current_board, ai_char);
		}
		
		// It's the "AI"'s turn, so we evaluate good positions
		// with positive values
		if(ai_turn)
		{
			best_move = -10000;
			
			for(int i = 0; i < current_board.length(); ++i)
			{
				if(current_board.charAt(i) == '-')
				{
					char[] char_board = current_board.toCharArray();
					
					char_board[i] = ai_char;
					
					String temp_board = new String(char_board);
					
					int temp_val = minMax(temp_board, ++depth, ai_char, !ai_turn);
					
					if(temp_val > best_move)
					{
						best_move = temp_val;
					}
					
				}
			}
		}
		else
		{
			// It's the player's turn, so we evaluate good positions
			// with negative values
			best_move = 10000;
			
			for(int i = 0; i < current_board.length(); ++i)
			{
				if(current_board.charAt(i) == '-')
				{
					char[] char_board = current_board.toCharArray();
					
					if(ai_char == 'X')
					{
						char_board[i] = 'O';
					}
					else
					{
						char_board[i] = 'X';
					}
					
					String temp_board = new String(char_board);
					
					int temp_val = minMax(temp_board, ++depth, ai_char, !ai_turn);
					
					if(temp_val < best_move)
					{
						best_move = temp_val;
					}
				}
			}
		}
		
		return best_move - depth;
	}
	
	// return a integer describing the state of the game from the "AI"'s PoV
	public int evaluate(String current_board, char ai_char)
	{
		// Draw
		if(!current_board.contains("-"))
		{
			return 0;
		}
		
		// Horizontal wins
		if( ( (current_board.charAt(0) == ai_char) && (current_board.charAt(1) == ai_char) && (current_board.charAt(2) == ai_char) ) ||
			( (current_board.charAt(3) == ai_char) && (current_board.charAt(4) == ai_char) && (current_board.charAt(5) == ai_char) ) ||
			( (current_board.charAt(6) == ai_char) && (current_board.charAt(7) == ai_char) && (current_board.charAt(8) == ai_char) ) )
		{
			return 100;
		}
		
		// Vertical wins
		if( ( (current_board.charAt(0) == ai_char) && (current_board.charAt(3) == ai_char) && (current_board.charAt(6) == ai_char) ) ||
			( (current_board.charAt(1) == ai_char) && (current_board.charAt(4) == ai_char) && (current_board.charAt(7) == ai_char) ) ||
			( (current_board.charAt(2) == ai_char) && (current_board.charAt(5) == ai_char) && (current_board.charAt(8) == ai_char) ) )
		{
			return 100;
		}
		
		// Diagonal wins
		if( ( (current_board.charAt(0) == ai_char) && (current_board.charAt(4) == ai_char) && (current_board.charAt(8) == ai_char) ) ||
			( (current_board.charAt(6) == ai_char) && (current_board.charAt(4) == ai_char) && (current_board.charAt(2) == ai_char) ) )
		{
			return 100;
		}
				
		return -100;		
	}
	
	// Checks if there are moves left
	public boolean moves_left(String current_board)
	{
		// Draw
		if(!current_board.contains("-"))
		{
			return false;
		}
		
		// Horizontal wins
		if( ( (current_board.charAt(0) != '-') && (current_board.charAt(0) == current_board.charAt(1)) && (current_board.charAt(1) == current_board.charAt(2)) ) ||
			( (current_board.charAt(3) != '-') && (current_board.charAt(3) == current_board.charAt(4)) && (current_board.charAt(4) == current_board.charAt(5)) ) ||
			( (current_board.charAt(6) != '-') && (current_board.charAt(6) == current_board.charAt(7)) && (current_board.charAt(7) == current_board.charAt(8)) ) )
		{
			return false;
		}
		
		// Vertical wins
		if( ( (current_board.charAt(0) != '-') && (current_board.charAt(0) == current_board.charAt(3)) && (current_board.charAt(3) == current_board.charAt(6)) ) ||
			( (current_board.charAt(1) != '-') && (current_board.charAt(1) == current_board.charAt(4)) && (current_board.charAt(4) == current_board.charAt(7)) ) ||
			( (current_board.charAt(2) != '-') && (current_board.charAt(2) == current_board.charAt(5)) && (current_board.charAt(5) == current_board.charAt(8)) ) )
		{
			return false;
		}
		
		// Diagonal wins
		if( ( (current_board.charAt(0) != '-') && (current_board.charAt(0) == current_board.charAt(4)) && (current_board.charAt(4) == current_board.charAt(8)) ) ||
			( (current_board.charAt(6) != '-') && (current_board.charAt(6) == current_board.charAt(4)) && (current_board.charAt(4) == current_board.charAt(2)) ) )
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString()
	{
		return "Game: id=" + this.id + ", board=" + this.board + ", state=" + this.status;
	}

	public String getId() {
		return id;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public StatusString getStatus() {
		return status;
	}	
}

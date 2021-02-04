package telegrambot;

import java.util.HashMap;

import telegrambot.InvestBotException.UserIncorrectScenarioException;

public class User {
	
	
	public static final int STATE_IDLE=1;
	public static final int STATE_WAIT_FOR_CHOICE_OF_OPERATION=2;
	public static final int STATE_WAIT_FOR_STOCK_TICKER = 3;
	public static final int STATE_WAIT_FOR_WATCH_PRICE = 4;
	private static HashMap<Integer, String> states = new HashMap<Integer, String>(){{
		put(STATE_IDLE, "STATE_IDLE");
		put(STATE_WAIT_FOR_CHOICE_OF_OPERATION, "STATE_WAIT_FOR_CHOICE_OF_OPERATION");
		put(STATE_WAIT_FOR_STOCK_TICKER, "STATE_WAIT_FOR_STOCK_TICKER");
		put(STATE_WAIT_FOR_WATCH_PRICE, "STATE_WAIT_FOR_WATCH_PRICE");
	}};
	
	public static final int SCENARIO_NONE=1;
	public static final int SCENARIO_ADD_STOCK_TO_PORTFOLIO = 2;
	public static final int SCENARIO_ADD_STOCK_TO_WATCHLIST = 3;
	private static HashMap<Integer, String> scenarios = new HashMap<Integer, String>(){{
		put(SCENARIO_NONE, "SCENARIO_NONE");
		put(SCENARIO_ADD_STOCK_TO_PORTFOLIO, "SCENARIO_ADD_STOCK_TO_PORTFOLIO");
		put(SCENARIO_ADD_STOCK_TO_WATCHLIST, "SCENARIO_ADD_STOCK_TO_WATCHLIST");
	}};

	public static String getStateNameById(int id) {
		return states.get(id);
	}
	
	public static String getScenarionNAmeById(int id) {
		return scenarios.get(id);
	}
	

//		{
//			new int[] = { STATE_IDLE, "STATE_IDLE"},
//			new int[] = { STATE_WAIT_FOR_STOCK_NAME, "STATE_WAIT_FOR_STOCK_NAME"},
//			new int[] = { STATE_WAIT_FOR_WATCH_PRICE, "STATE_WAIT_FOR_WATCH_PRICE"},
//			new int[] = { STATE_WAIT_FOR_CHOICE_OF_OPERATION, "STATE_WAIT_FOR_CHOICE_OF_OPERATION"},
//	};

	
	private String userName;
	private int state;
	private int scenario;
	
	private ObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST objectSCENARIO_ADD_STOCK_TO_WATCHLIST=null;
	
	public User(String userName) {
		this.userName=userName;
		this.state=STATE_IDLE;
		this.scenario=SCENARIO_NONE;
	}
	
	public ObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST getObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST() {
		return objectSCENARIO_ADD_STOCK_TO_WATCHLIST;
	}
	
	public void setObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST(ObjectForSCENARIO_ADD_STOCK_TO_WATCHLIST objectSCENARIO_ADD_STOCK_TO_WATCHLIST) {
		this.objectSCENARIO_ADD_STOCK_TO_WATCHLIST=objectSCENARIO_ADD_STOCK_TO_WATCHLIST;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setScenario(int scenario) throws UserIncorrectScenarioException {
		boolean allowedScenario=false;;
		for(int i : scenarios.keySet()) {
			if(i==scenario) {
				allowedScenario=true;
				break;
			}
		}
		if(allowedScenario) {
			this.scenario = scenario;
		}else {
			throw new InvestBotException.UserIncorrectScenarioException(scenario);
		}
	}
	public int getScenario() {
		return scenario;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) throws InvestBotException.UserIncorrectStateException {
		boolean allowedState=false;;
		for(int i : states.keySet()) {
			if(i==state) {
				allowedState=true;
				break;
			}
		}
		if(allowedState) {
			this.state = state;
		}else {
			throw new InvestBotException.UserIncorrectStateException(state);
		}
	}
	public boolean equals(User user) {
		return this.userName.equals(user.getUserName());
	}
}
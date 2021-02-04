package telegrambot;


public class InvestBotException extends Exception{
	private InvestBotException(String text) {
		super(text);
	}
	
	
	public static class UserNotFoundException extends InvestBotException{
		public UserNotFoundException(String userName) {
			super("User ["+userName+"] not found");
		}
	}
	
	public static class UserIncorrectStateException extends InvestBotException{
		public UserIncorrectStateException(int state) {
			super("User state ["+state+"] is incorrect");
		}
	}
	
	public static class UserIncorrectScenarioException extends InvestBotException{
		public UserIncorrectScenarioException(int scenario) {
			super("User scenario ["+scenario+"] is incorrect");
		}
	}

	public static class GetURLContentException extends InvestBotException{
		public GetURLContentException(String text, String url) {
			super(String.format("An errror was occured during getting a content of the [%s] url. Description: %s",  url, text));
		}
	}
	
}

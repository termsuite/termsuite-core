package eu.project.ttc.metrics;

public class TextExplanation implements IExplanation {
	private String text;

	public TextExplanation(String text) {
		this.text = text;
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		return getText();
	}
	

}

package languagemodel;
import std.StdIn;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class LanguageModel {

	// The length of the moving window
	private int windowLength; 
	
	// The map for managing the (window, LinkedList) mappings 
	private HashMap<String, LinkedList<CharProb>> probabilities;

	// Random number generator:
	// Used by the getRandomChar method, initialized by the class constructors. 
	Random randomGenerator;
	
	/**
	 * Creates a new language model, using the given window length
	 * and a given (fixed) number generator seed.
	 * @param windowLength
	 * @param seed
	 */
	public LanguageModel(int windowLength, int seed) {
		this.randomGenerator = new Random(seed);
		this.windowLength = windowLength;
		probabilities = new HashMap<String, LinkedList<CharProb>>();
	}	
	
	/**
	 * Creates a new language model, using the given window length
	 * and a random number generator seed.
	 * @param windowLength
	 */
	public LanguageModel(int windowLength) {
		this.randomGenerator = new Random();
		this.windowLength = windowLength;
		probabilities = new HashMap<String, LinkedList<CharProb>>();
	}

	/**
	 * Builds a language model from the text in standard input (the corpus).
	 */
	public void train() {
		
		String window = "";
		char c;
		c = StdIn.readChar();
		for(int i = 0; i < windowLength; i++) {
			window += StdIn.readChar();
		}
		while (!StdIn.isEmpty()) {
			c = StdIn.readChar();
			LinkedList<CharProb> probs = probabilities.get(window);
			if (probs == null) {
				probs = new LinkedList<CharProb>();
				probabilities.put(window,probs);	
			}
			calculateCounts(probs, c);
			window = window.substring(1) + c;
		}

		for (LinkedList<CharProb> probs : probabilities.values()) 
			calculateProbabilities(probs);
	}

	// Calculates the counts of the current character.
	private void calculateCounts(LinkedList<CharProb> probs, char c) {
		boolean inTheList = false;
		for(int i = 0; i < probs.size(); i++) {
			if(probs.get(i).chr == c) {
				probs.get(i).count ++;
				inTheList = true;
			}
		}
		if(! inTheList) {
			probs.add(new CharProb(c));
		}
	}

	// Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	private void calculateProbabilities(LinkedList<CharProb> probs) {				
		int totalcount = 0;
		for(int i = 0; i < probs.size() ; i++) {
			totalcount += probs.get(i).count;
		}
		probs.get(0).p = probs.get(0).count / (double) totalcount;
		probs.get(0).cp = probs.get(0).count / (double) totalcount;
		for(int i = 1; i < probs.size() ; i++) {
			probs.get(i).p = probs.get(i).count / (double) totalcount;
			probs.get(i).cp = (probs.get(i-1).cp) + probs.get(i).p ;
		}	
	}	

	/**
	 * Returns a string representing the probabilities map.
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : probabilities.keySet()) {
			LinkedList<CharProb> keyProbs = probabilities.get(key);
			str.append( key + " : ");
			str.append("(");
			for(int i = 0; i < keyProbs.size() ; i ++) {
				str.append(keyProbs.get(i).toString());
			}
			str.append( ")");
			str.append("\n");		}
		return str.toString();
	}	

	/**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start. 
	 * @param textLength - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if(initialText.length() < this.windowLength) {
			return initialText;
		}
		else{
			String GenerateText = initialText;
			String Window = initialText;
			char random;
			while(GenerateText.length() < textLength) {
				LinkedList<CharProb> probs = probabilities.get(Window);
				if(probs == null) {
					return GenerateText;
				}
				random = getRandomChar(probs);
				GenerateText += random;
				Window = Window.substring(1) + random;
			}	
			return GenerateText ;
		}
		
	}

	// Returns a random character from the given probabilities list.
	public char getRandomChar(LinkedList<CharProb> probs) {
		double random = randomGenerator.nextDouble();
		int i = 0;
		char randomChar = ' ';
		while(random > (probs.get(i)).cp ) {
			i++;
		}
		randomChar = (probs.get(i)).chr;
		return randomChar;

	}
	
	/**
	 * A Test of the LanguageModel class.
	 * Learns a given corpus (text file) and generates text from it.
	 */
	public static void main(String []args) {		
		int windowLength = Integer.parseInt(args[0]);  // window length
		String initialText = args[1];			      // initial text
		int textLength = Integer.parseInt(args[2]);	  // size of generated text
		boolean random = (args[3].equals("random") ? true : false);  // random / fixed seed
		StdIn.setInput("shakespeareinlove.txt");
		LanguageModel lm;

		// Creates a language model with the given window length and random/fixed seed
		if (random) {
			// the generate method will use a random seed
			lm = new LanguageModel(windowLength);      
		} else {
			// the generate method will use a fixed seed = 20 (for testing purposes)
			lm = new LanguageModel(windowLength, 20); 
		}
		
		// Trains the model, creating the map.
		lm.train();
		
		// Generates text, and prints it.
		System.out.println(lm.generate(initialText,textLength));
	}
}

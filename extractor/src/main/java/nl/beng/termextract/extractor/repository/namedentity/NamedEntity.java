package nl.beng.termextract.extractor.repository.namedentity;


public class NamedEntity {

	private NamedEntityType type;
	private String text;

	public NamedEntityType getType() {
		return type;
	}

	public void setType(NamedEntityType type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "NamedEntity [type=" + type + ", text=" + text + "]";
	}
	
}

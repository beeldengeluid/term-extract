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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NamedEntity other = (NamedEntity) obj;
        if (text == null) {
            if (other.text != null) return false;
        } else if (!text.equals(other.text)) return false;
        if (type != other.type) return false;
        return true;
    }
	
}

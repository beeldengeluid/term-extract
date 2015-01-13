package nl.beng.termextract.extractor.repository.namedentity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    @JsonInclude(Include.NON_NULL)
    private int id;
    @JsonInclude(Include.NON_NULL)
    private String word;
    @JsonInclude(Include.NON_NULL)
    private String lemma;
    @JsonInclude(Include.NON_NULL)
    private String pos;
    @JsonInclude(Include.NON_NULL)
    private String pos1;
    @JsonInclude(Include.NON_NULL)
    private Object rel;
    @JsonInclude(Include.NON_NULL)
    private String ne;
    @JsonInclude(Include.NON_NULL)
    private int sentence;
    @JsonProperty("pos_confidence")
    @JsonInclude(Include.NON_NULL)
    private int confidence;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String getLemma() {
        return lemma;
    }
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }
    public String getPos() {
        return pos;
    }
    public void setPos(String pos) {
        this.pos = pos;
    }
    public String getPos1() {
        return pos1;
    }
    public void setPos1(String pos1) {
        this.pos1 = pos1;
    }
    public int getSentence() {
        return sentence;
    }
    public void setSentence(int sentence) {
        this.sentence = sentence;
    }
    public int getConfidence() {
        return confidence;
    }
    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
    public Object getRel() {
        return rel;
    }
    public void setRel(Object rel) {
        this.rel = rel;
    }
    public String getNe() {
        return ne;
    }
    public void setNe(String ne) {
        this.ne = ne;
    }
}

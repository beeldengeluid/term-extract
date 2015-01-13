package nl.beng.termextract.extractor.repository.namedentity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Saf {
    private List<Token> tokens;
    
    private Header header;
    
    public static class Header {
        private String format;
        
        @JsonProperty("format-version")
        private String formatVersion;
        
        private List<Module> processed;
        
        public String getFormat() {
            return format;
        }
        public void setFormat(String format) {
            this.format = format;
        }
        public String getFormatVersion() {
            return formatVersion;
        }
        public void setFormatVersion(String formatVersion) {
            this.formatVersion = formatVersion;
        }
        public List<Module> getProcessed() {
            return processed;
        }
        public void setProcessed(List<Module> processed) {
            this.processed = processed;
        }
        
        public static class Module{
            private String started;
            private String module;
            
            public String getStarted() {
                return started;
            }
            public void setStarted(String started) {
                this.started = started;
            }
            public String getModule() {
                return module;
            }
            public void setModule(String module) {
                this.module = module;
            }
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}

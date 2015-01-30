term-extract
============

Term extractie uit de Gemeenschappelijke Thesaurus Audiovisuele Archieven en andere bronnen.

The available values for "namedentity.repository" in the "settings" can be 

        "xtas" : for 904labas xtas
        "xtas-local" : for local xtas installed in beeldengeluid
        "cltl"

#### Example termextract POST

    curl -H 'Content-Type: application/json' -X POST 'http://labs-test.beeldengeluid.nl/termextract' -d 
    '{
    "settings": {
        "tokenizer.min.norm.frequency": "0.000004",
        "namedentity.organization.min.score": "8",
        "namedentity.repository": "cltl",
        "tokenizer.max.gram": "3",
        "tokenizer.min.score": "8",
        "tokenizer.min.gram": "2",
        "namedentity.organization.min.token.frequency": "2",
        "namedentity.person.min.score": "8",
        "namedentity.misc.min.score": "8",
        "namedentity.location.min.token.frequency": "2",
        "namedentity.min.token.frequency": "2",
        "namedentity.location.min.score": "8",
        "namedentity.person.min.token.frequency": "1",
        "namedentity.misc.min.token.frequency": "2",
        "tokenizer.min.token.frequency": "2"
    },
    "text": "Mooie goal van Nistelrooy van Ruud. 1-0 voor Manchester United. John Jones Mary and Mr. J. J. Jones ran to         Washington. mensen"
      }'
      
      
     {
    "version": "1.1",
    "total": 3,
    "settings": {
        "tokenizer.min.norm.frequency": "0.000004",
        "namedentity.organization.min.score": "8",
        "tokenizer.max.gram": "3",
        "tokenizer.min.score": "8",
        "tokenizer.min.gram": "2",
        "namedentity.organization.min.token.frequency": "2",
        "namedentity.person.min.score": "8",
        "namedentity.misc.min.score": "8",
        "namedentity.repository": "cltl",
        "namedentity.location.min.token.frequency": "2",
        "namedentity.min.token.frequency": "2",
        "namedentity.location.min.score": "8",
        "namedentity.person.min.token.frequency": "1",
        "namedentity.misc.min.token.frequency": "2",
        "tokenizer.min.token.frequency": "2"
    },
    "matches": [
        {
        "uri": "http://data.beeldengeluid.nl/gtaa/60826",
        "type": "namen",
        "score": 12.484395,
        "pref_label": "Manchester United"
        },
        {
        "uri": "http://data.beeldengeluid.nl/gtaa/136992",
        "type": "persoonsnamen",
        "score": 10.639112,
        "pref_label": "Nistelrooy, Ruud van"
        },
        {
        "uri": "http://data.beeldengeluid.nl/gtaa/238092",
        "type": "persoonsnamen",
        "score": 8.6288595,
        "pref_label": "Mr. Probz",
        "alt_label": "Stehr, Dennis Princewell",
        "concept_schemes": [
            "http://data.beeldengeluid.nl/gtaa/Persoonsnamen"
          ]
        }]
}
    
    
    

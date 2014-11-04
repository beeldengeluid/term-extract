term-extract
============

Term extractie uit de Gemeenschappelijke Thesaurus Audiovisuele Archieven en andere bronnen.


#### Example termextract POST

    curl -X POST http://labs.beeldengeluid.nl/termextract
     {"text": "Nelson mandela was een Zuid-Afrikaans anti-apartheidstrijder en politicus. Vanaf 1944 was Mandela betrokken bij de strijd van het Afrikaans Nationaal Congres (ANC) tegen het apartheidsregime in Zuid-Afrika. Als leider van de militaire tak van het ANC werd hij in 1963 opgepakt en kreeg hij een levenslange gevangenisstraf opgelegd. In 1990 kwam hij vrij en werd het ANC gelegaliseerd. Samen met president F.W. de Klerk kreeg Mandela in 1993 de Nobelprijs voor de Vrede voor \"hun inspanningen voor het vreedzaam einde van het apartheidsregime en het leggen van de funderingen voor een nieuw democratisch Zuid-Afrika\"."}
    
    
    [
     {
      "uri": "http://data.beeldengeluid.nl/gtaa/45268",
      "type": "geografischenamen",
      "pref_label": "Zuid-Afrika",
      "concept_schemes": [
         "http://data.beeldengeluid.nl/gtaa/GeografischeNamen",
         "http://data.beeldengeluid.nl/gtaa/GTAA"
      ]
     },
    {
      "uri": "http://data.beeldengeluid.nl/gtaa/46529",
      "type": "namen",
      "pref_label": "ANC",
      "alt_label": "African National Congress",
      "concept_schemes": [
        "http://data.beeldengeluid.nl/gtaa/Namen",
        "http://data.beeldengeluid.nl/gtaa/GTAA"
      ]
     }
    ]

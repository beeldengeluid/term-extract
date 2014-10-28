package nl.beng.termextract.extractor.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.beng.termextract.extractor.model.Term;
import nl.beng.termextract.extractor.repository.GtaaRepository;
import nl.beng.termextract.extractor.service.ExtractorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtractorServiceImpl implements ExtractorService {

	@Autowired
	private GtaaRepository repository;

	@Override
	public List<Set<Term>> extract(List<String> texts) {
		List<Set<Term>> extractedTermsList = new LinkedList<>();
		
		return extractedTermsList;
	}

}

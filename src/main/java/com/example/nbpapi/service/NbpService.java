package com.example.nbpapi.service;

import com.example.nbpapi.model.NbpResponse;
import com.example.nbpapi.repository.NbpResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.nbpapi.model.NbpResponse;
import com.example.nbpapi.model.Rate;
import com.example.nbpapi.model.Root;
import com.example.nbpapi.repository.NbpResponseRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NbpService {

    private final RestTemplate restTemplate;
    private final NbpResponseRepository nbpResponseRepository;

    public NbpService(RestTemplate restTemplate, NbpResponseRepository nbpResponseRepository) {
        this.restTemplate = restTemplate;
        this.nbpResponseRepository = nbpResponseRepository;
    }

    public NbpResponse calculateRootForCurrency(String currency, int numberOfDays) {
        String url = "http://api.nbp.pl/api/exchangerates/rates/a/" + currency + "/last/" + numberOfDays;
        Root root = restTemplate.getForObject(url, Root.class);
        double average = calculate(root.getRates());
        NbpResponse nbpResponse = getNbpResponse(currency, numberOfDays, average);
        return nbpResponseRepository.save(nbpResponse);
    }

    private NbpResponse getNbpResponse(String currency, int numberOfDays, double calculate) {
        NbpResponse nbpResponse = new NbpResponse();
        nbpResponse.setCurrency(currency);
        nbpResponse.setDays(numberOfDays);
        nbpResponse.setAverage(calculate);
        nbpResponse.setCreatedAt(LocalDateTime.now());
        return nbpResponse;
    }

    public double calculate(List<Rate> rateList) {
        return rateList.stream()
                .mapToDouble(Rate::getMid)
                .average()
                .orElse(0.0d);
    }
}

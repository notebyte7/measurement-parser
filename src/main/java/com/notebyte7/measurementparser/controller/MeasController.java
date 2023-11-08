package com.notebyte7.measurementparser.controller;

import com.notebyte7.measurementparser.service.MeasService;
import com.notebyte7.measurementparser.repository.MeasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "/meas")
@RequiredArgsConstructor
public class MeasController {
    private final MeasService measService;
    private final MeasRepository measRepository;

    @GetMapping("/parse")
    public void parseMeas() {
        measService.parseAllFilesFromFolder("/Users/ilya/test");
    }

    @GetMapping("/index")
    public String showMeasOnMap(Model model) {
        model.addAttribute("measures", measRepository.findAll());
        return "index";
    }
}

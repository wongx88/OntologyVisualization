package com.itls.ontologyrest.controller;

import com.itls.ontologyrest.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class OntologySvcController {

    @Autowired
    private OntologyService ontologyService;

    @RequestMapping(value = "/customers", produces = "text/csv")
    public String getCustomerCSV(HttpServletResponse response) throws IOException {
        //  response.getWriter().write();
        return ontologyService.getCustomerCSV();
    }

    @RequestMapping(value = "/getRecordsBySSN")
    public String getCustomerBySSN(@RequestParam(value = "SSN", required = false) String SSN) {
        if (SSN != null) {
            return ontologyService.getHEBJSON(SSN);
        } else
            return "";
    }

    @RequestMapping("/heb")
    public String getHEBJSON() {
        return ontologyService.getHEBJSON();
    }

    @RequestMapping("/circlePacking")
    public String getCPJSON() {
        return ontologyService.getCirclePackingJSON();
    }

    @RequestMapping("/fdg")
    public String getFDGJSON(@RequestParam(required = false) String type) {
        if (type != null && type.toLowerCase().equals("household"))
            return ontologyService.getHouseHoldFDG();
        else
            return ontologyService.getFDGJSON();
    }
}
package com.itls.ontologyrest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class InputConfig {

    private final static Logger logger = LoggerFactory.getLogger(InputConfig.class);

    @Value("classpath:dataset/PropensityToBuyScore_7_04_2020.csv")
    private Resource customerData;

    @Bean(name = "customer")
    public Resource getCustomerData() {
        return customerData;
    }
//
//    @Bean(name="mapping")
//    public Map<Integer,Character> getMapping() {
//        Map<Integer,Character> mapping = new HashMap<>();
//        try(Scanner sc = new Scanner(file.getInputStream())) {
//            while(sc.hasNextLine()){
//                mapping.put(sc.nextInt(),sc.next().charAt(0));
//            }
//        } catch (IOException e) {
//            logger.error("could not load mapping file",e)
//        }
//        return mapping;
//    }

}

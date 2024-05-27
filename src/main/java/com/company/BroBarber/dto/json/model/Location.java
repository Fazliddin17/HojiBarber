package com.company.BroBarber.dto.json.model;

import lombok.Data;

@Data
public class Location {
    private String lat;
    private String lon;
    private String display_name ;
    private Address address;
}

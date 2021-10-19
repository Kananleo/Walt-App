package com.walt;

import com.walt.Exceptions.NoAvailableDriversException;
import com.walt.Exceptions.NoCustomerException;
import com.walt.Exceptions.NotSameCityException;
import com.walt.model.*;

import java.util.Date;
import java.util.List;

public  interface WaltService{

    Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws NotSameCityException, NoAvailableDriversException, NoCustomerException;

    List<DriverDistance> getDriverRankReport();

    List<DriverDistance> getDriverRankReportByCity(City city);
}


package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DriverRepository extends CrudRepository<Driver,Long> {
    List<Driver> findAllDriversByCity(City city);
    Driver findByName(String name);
    @Query(
        "select driver from Driver driver where driver.city = :city and driver.id not in" + 
        "(select driver2.id from Driver driver2," + 
        "Delivery delivery where delivery.driver = driver2 and delivery.deliveryTime = :deliveryTime and driver2.city = :city)"
    )
    List<Driver> getAvailableDrivers(City city, Date deliveryTime);
    
    @Query(
            "select delivery.driver, sum(delivery.distance) as totalDistance" +
            " from Delivery delivery" +
            " group by delivery.driver" +
            " order by totalDistance desc"
    )
    List<DriverDistance> getDistancesByDriver();

    @Query(
            "select delivery.driver, sum(delivery.distance) as totalDistance" +
            " from Delivery delivery where delivery.driver.city = :city" +
            " group by delivery.driver" +
            " order by totalDistance desc"
    )
    List<DriverDistance> getDriverDistancesByCity(City city);
}

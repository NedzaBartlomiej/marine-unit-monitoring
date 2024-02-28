package pl.bartlomiej.marineunitmonitoring.ais;

import java.util.Date;

public record Properties(
        Long mmsi,
        String name,
        Date msgtime,
        Double speedOverGround,
        Double courseOverGround,
        Integer navigationalStatus,
        Integer rateOfTurn,
        Integer shipType,
        Integer trueHeading,
        String callSign,
        String destination,
        String eta,
        Integer imoNumber,
        Integer dimensionA,
        Integer dimensionB,
        Integer dimensionC,
        Integer dimensionD,
        Integer draught,
        Integer shipLength,
        Integer shipWidth,
        Integer positionFixingDeviceType,
        String reportClass) {
}
package pl.bartlomiej.marineunitmonitoring.ais;

import java.util.Date;

public record Properties(
        int mmsi,
        String name,
        Date msgtime,
        double speedOverGround,
        double courseOverGround,
        int navigationalStatus,
        int rateOfTurn,
        int shipType,
        int trueHeading,
        String callSign,
        String destination,
        String eta,
        int imoNumber,
        int dimensionA,
        int dimensionB,
        int dimensionC,
        int dimensionD,
        int draught,
        int shipLength,
        int shipWidth,
        int positionFixingDeviceType,
        String reportClass) {
}
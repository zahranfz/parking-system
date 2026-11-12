package com.parkingsystem.common;

/**
 * Interface lintas layer untuk objek yang dapat diserialisasi menjadi satu baris CSV.
 */
public interface CsvWritable {

    /**
     * Mengubah data objek menjadi format satu baris CSV.
     */
    String toCsvRow();
}


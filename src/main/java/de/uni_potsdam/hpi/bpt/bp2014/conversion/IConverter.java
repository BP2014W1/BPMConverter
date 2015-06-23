package de.uni_potsdam.hpi.bpt.bp2014.conversion;

/**
 * An interface for the converter.
 * A generic implementation to convert between different Process Model representations.
 * If you transform exactly one process model into exactly one model of another
 * representation you may use this interface.
 */
public interface IConverter<T1 extends IModel, T2 extends IModel> {

    /**
     * This method should convert the model to a new model of type t/T.
     * @param model The model to be transformed.
     * @return The generated model.
     */
    T2 convert(T1 model);
}

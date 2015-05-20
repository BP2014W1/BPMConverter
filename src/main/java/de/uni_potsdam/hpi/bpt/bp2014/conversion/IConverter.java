package de.uni_potsdam.hpi.bpt.bp2014.conversion;

/**
 * An interface for the converter.
 * A generic implementation to convert between different Process Model representations.
 * If you transform exactly one process model into exactly one model of another
 * representation you may use this interface.
 */
public interface IConverter {

    /**
     * This method should convert the model to a new model of type t/T.
     * @param model The model to be transformed.
     * @param t The type of the new model.
     * @param <T> The returned type.
     * @return
     */
    <T extends IModel> T convert(IModel model, Class<T> t);
}

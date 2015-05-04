package de.uni_potsdam.hpi.bpt.bp2014.conversion;

public interface IConverter {

    <T extends IModel> T convert(IModel model, Class<T> t);
}

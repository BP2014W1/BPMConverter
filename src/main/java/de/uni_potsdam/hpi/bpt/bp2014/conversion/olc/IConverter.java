package de.uni_potsdam.hpi.bpt.bp2014.conversion.olc;

import de.uni_potsdam.hpi.bpt.bp2014.conversion.IModel;

public interface IConverter {
    public <T extends IModel> T convert(IModel model, Class T);
}

# JConverter

This repository contains a project for converting and synchronizing different Business Process Model representations.
If you would like to learn more about this topic you might want to read [this paper (PDF)](http://bpt.hpi.uni-potsdam.de/pub/Public/AndreasMeyer/Activity-centric_and_Artifact-centric_Process_Model_Roundtrip.pdf).

## Goal

The projects goal is to provide a suite of converters to convert Business Process Models from one representation into another.
And to use this method to synchronize multiple representations of one Process Model.

## Build

To build the project simply run *maven*.

    mvn clean install  

## Usage

**Please note: This project is still under development. It is highly unstable and we recommend not to use this project.**

You can use the classes inside the model package to create a model.
If the model is valid it can be used by the converters to create another representation.

## License

This project is 100% open source and published under the MIT License. For more information see the [license file](license.md)
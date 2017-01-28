# Barracks + Nordic DFU exemple #
This  exemple app shows you how to push an update from [Barracks](https://barracks.io/) to a [Nordic NRF52](https://www.nordicsemi.com/Products/nRF52-Series-SoC) board, using the NordicDFU library and the Barracks SDK.
![Nordic NRF52](https://www.nordicsemi.com/var/ezwebin_site/storage/images/media/images/products/nrf52-preview-dk/1481117-1-eng-GB/nRF52-Preview-DK_large.jpg)
## You should take a look at : ##
* The [Android Barracks SDK](https://github.com/barracksiot/android-client)
* The [Nordic Android DFULibrary](https://github.com/NordicSemiconductor/Android-DFU-Library)

## Setup ##

* Download the source code
* Sync your build.gradle 🙂


## Using Barracks and Nordic ##

* Don't forget to enter your API key in the BarracksHelper init

## How it works ? ##

* 1 : Find the advertising NRF52 by scanning bluetooth device
* 2 : Once we found the device, let use Barracks to check if an update is available for the versionID we wrote.
* 3 : If an update is available, let's proceed to the installation by downloading update using BarracksHelper then push it on the NRF52 to update the firmware using the Nordic DFU Library.

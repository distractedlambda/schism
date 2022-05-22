#![feature(maybe_uninit_uninit_array, maybe_uninit_slice, once_cell)]

use anyhow::Result;
use clap::Parser;

mod libusb;
mod picoboot;

#[derive(Parser, Debug)]
#[clap(author, version, about, long_about = None)]
enum Cli {
    #[clap(about = "List recognized devices")]
    ListDevices,
}

fn main() -> Result<()> {
    let args = Cli::parse();
    match args {
        Cli::ListDevices => {
            let context = libusb::Context::new()?;
            let devices: Vec<_> = context.get_devices()?;

            for device in devices {
                let device_descriptor = if let Ok(desciptor) = device.get_descriptor() {
                    desciptor
                } else {
                    continue;
                };

                if !picoboot::device_descriptor_matches(&device_descriptor) {
                    continue;
                }

                let configuration_descriptor =
                    if let Ok(descriptor) = device.get_active_configuration_descriptor() {
                        descriptor
                    } else {
                        continue;
                    };

                let picoboot_interface =
                    if let Some(interface) = picoboot::find_interface(&configuration_descriptor) {
                        interface
                    } else {
                        continue;
                    };

                println!("{:#?}", picoboot_interface)
            }

            Ok(())
        }
    }
}

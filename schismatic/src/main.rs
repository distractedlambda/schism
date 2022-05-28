#![feature(maybe_uninit_uninit_array, maybe_uninit_slice, once_cell)]

use anyhow::Result;
use clap::Parser;

mod libusb;
mod picoboot;

#[derive(Parser, Debug)]
#[clap(author, version, about, long_about = None)]
enum Cli {
    #[clap(about = "Follow the log output of a Schism-based device")]
    FollowLog,
}

fn main() -> Result<()> {
    let args = Cli::parse();
    match args {
        Cli::FollowLog => {
            let context = libusb::Context::new()?;
            let devices: Vec<_> = context.get_devices()?;

            for device in devices {
                let configuration_descriptor =
                    if let Ok(descriptor) = device.get_active_configuration_descriptor() {
                        descriptor
                    } else {
                        continue;
                    };

                for interface in configuration_descriptor.interfaces {
                }
            }

            Ok(())
        }
    }
}

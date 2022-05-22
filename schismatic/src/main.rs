#![feature(maybe_uninit_uninit_array, maybe_uninit_slice, once_cell)]

use anyhow::Result;
use clap::Parser;

mod libusb;

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
                println!("{:#?}", device.get_descriptor()?)
            }
            Ok(())
        }
    }
}

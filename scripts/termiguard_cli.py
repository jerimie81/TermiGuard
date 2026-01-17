import argparse

from modules.scrcpy_manager import ScrcpyLauncher


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="TermiGuard utility CLI")
    subparsers = parser.add_subparsers(dest="command", required=True)

    mobile_parser = subparsers.add_parser("mobile", help="Launch scrcpy session")
    mobile_parser.add_argument("--device", help="ADB device ID")
    mobile_parser.add_argument("args", nargs="*", help="Additional scrcpy args")
    return parser


def main() -> None:
    parser = build_parser()
    args = parser.parse_args()

    if args.command == "mobile":
        launcher = ScrcpyLauncher()
        launcher.run_scrcpy(device_id=args.device, extra_args=args.args)


if __name__ == "__main__":
    main()
